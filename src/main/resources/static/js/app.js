var API = "https://resourceplanningledger-1.onrender.com";
var currentPlanId = null;
var currentActionId = null;
var stepCount = 0;

function showPage(page, tabElement) {
    var pages = document.querySelectorAll(".page");
    for (var i = 0; i < pages.length; i++) { pages[i].classList.remove("active"); }
    var tabs = document.querySelectorAll(".tab");
    for (var i = 0; i < tabs.length; i++) { tabs[i].classList.remove("active"); }
    document.getElementById("page-" + page).classList.add("active");
    tabElement.classList.add("active");
    loadPageData(page);
}

function loadPageData(page) {
    if (page === "dashboard") { loadDashboard(); }
    if (page === "protocols") { loadProtocols(); }
    if (page === "plans") { loadPlans(); }
    if (page === "ledger") { loadLedger(); }
    if (page === "audit") { loadAudit(); }
}

function api(method, path, body) {
    var opts = { method: method, headers: { "Content-Type": "application/json" } };
    if (body) { opts.body = JSON.stringify(body); }
    return fetch(API + path, opts).then(function(res) {
        if (!res.ok) {
            return res.json().catch(function() { return {}; }).then(function(err) {
                alert(err.message || "Error");
                throw new Error(err.message);
            });
        }
        if (res.status === 204) { return null; }
        return res.json();
    });
}

function loadDashboard() {
    api("GET", "/api/accounts").then(function(accounts) {
        var poolAccounts = accounts.filter(function(a) { return a.kind === "POOL"; });
        var alertSection = document.getElementById("alert-section");
        var belowZero = poolAccounts.filter(function(a) { return parseFloat(a.balance) < 0; });
        if (belowZero.length > 0) {
            alertSection.innerHTML = '<div class="alert-banner"><strong>Warning: Over-consumption Alert</strong>' +
                belowZero.map(function(a) { return a.name + ": " + a.balance; }).join(", ") + "</div>";
        } else { alertSection.innerHTML = ""; }

        document.getElementById("pool-balances").innerHTML =
            poolAccounts.map(function(a) {
                return "<tr><td>" + a.name + "</td><td>" + (a.resourceTypeName || "-") + "</td>" +
                    '<td class="' + (parseFloat(a.balance) < 0 ? "balance-alert" : "balance-ok") + '">' + a.balance + "</td></tr>";
            }).join("") || '<tr><td colspan="3" class="empty-state">No pool accounts yet</td></tr>';

        return api("GET", "/api/plans");
    }).then(function(plans) {
        document.getElementById("plans-overview").innerHTML =
            plans.map(function(p) {
                return "<tr><td>" + p.name + '</td><td><span class="status status-' + p.status + '">' + p.status + "</span></td>" +
                    "<td>" + p.actionCount + " actions, " + p.subPlanCount + " sub-plans</td></tr>";
            }).join("") || '<tr><td colspan="3" class="empty-state">No plans yet</td></tr>';
        return api("GET", "/api/resource-types");
    }).then(function(rts) {
        document.getElementById("resource-types-table").innerHTML =
            rts.map(function(rt) {
                return "<tr><td>" + rt.id + "</td><td>" + rt.name + "</td><td>" + rt.kind + "</td><td>" + rt.unit + "</td>" +
                    "<td>" + (rt.poolAccountId || "-") + "</td></tr>";
            }).join("") || '<tr><td colspan="5" class="empty-state">No resource types</td></tr>';
    });
}

function createResourceType() {
    var name = document.getElementById("rt-name").value;
    var kind = document.getElementById("rt-kind").value;
    var unit = document.getElementById("rt-unit").value;
    var balance = parseFloat(document.getElementById("rt-balance").value) || 0;
    if (!name || !unit) { alert("Name and unit required"); return; }
    api("POST", "/api/resource-types", { name: name, kind: kind, unit: unit, initialBalance: balance }).then(function() {
        document.getElementById("rt-name").value = "";
        document.getElementById("rt-unit").value = "";
        document.getElementById("rt-balance").value = "";
        loadDashboard();
    });
}

function addStepField() {
    stepCount++;
    var div = document.createElement("div");
    div.className = "form-row";
    div.style.marginBottom = "6px";
    div.innerHTML = '<div class="form-group"><input placeholder="Step name" id="step-name-' + stepCount + '"></div>' +
        '<div class="form-group"><input placeholder="Depends on (comma-sep)" id="step-deps-' + stepCount + '"></div>';
    document.getElementById("steps-container").appendChild(div);
}

function createProtocol() {
    var name = document.getElementById("proto-name").value;
    var desc = document.getElementById("proto-desc").value;
    if (!name) { alert("Protocol name required"); return; }
    var steps = [];
    for (var i = 1; i <= stepCount; i++) {
        var sName = document.getElementById("step-name-" + i);
        if (sName && sName.value) {
            var deps = document.getElementById("step-deps-" + i).value;
            steps.push({ name: sName.value,
                dependsOn: deps ? deps.split(",").map(function(s) { return s.trim(); }).filter(function(s) { return s; }) : []
            });
        }
    }
    api("POST", "/api/protocols", { name: name, description: desc, steps: steps }).then(function() {
        document.getElementById("proto-name").value = "";
        document.getElementById("proto-desc").value = "";
        document.getElementById("steps-container").innerHTML = "";
        stepCount = 0;
        loadProtocols();
    });
}

function loadProtocols() {
    api("GET", "/api/protocols").then(function(protocols) {
        document.getElementById("protocols-list").innerHTML =
            protocols.map(function(p) {
                return '<div style="border:1px solid #eee; padding:12px; margin-bottom:8px; border-radius:4px;">' +
                    "<strong>" + p.name + '</strong> <span style="color:#999;">(ID: ' + p.id + ")</span>" +
                    '<p style="color:#666; margin:4px 0;">' + (p.description || "No description") + "</p>" +
                    '<div style="font-size:13px; color:#555;">Steps: ' +
                    (p.steps.map(function(s) {
                        return s.name + (s.dependsOn && s.dependsOn.length ? " depends on: " + s.dependsOn.join(", ") : "");
                    }).join(" | ") || "none") + "</div></div>";
            }).join("") || '<div class="empty-state">No protocols yet</div>';
        var select = document.getElementById("plan-protocol");
        if (select) {
            select.innerHTML = '<option value="">-- From scratch --</option>' +
                protocols.map(function(p) { return '<option value="' + p.id + '">' + p.name + "</option>"; }).join("");
        }
    });
}

function loadPlans() {
    loadProtocols();
    api("GET", "/api/plans").then(function(plans) {
        document.getElementById("plans-list").innerHTML =
            plans.map(function(p) {
                return '<div style="border:1px solid #eee; padding:12px; margin-bottom:8px; border-radius:4px; cursor:pointer;" ' +
                    'onclick="loadPlanDetail(' + p.id + ')">' +
                    "<strong>" + p.name + "</strong>" +
                    '<span class="status status-' + p.status + '" style="margin-left:8px;">' + p.status + "</span>" +
                    '<span style="color:#999; margin-left:8px;">' + p.actionCount + " actions</span></div>";
            }).join("") || '<div class="empty-state">No plans yet</div>';
    });
}

function createPlan() {
    var name = document.getElementById("plan-name").value;
    var protocolId = document.getElementById("plan-protocol").value;
    if (!name) { alert("Plan name required"); return; }
    var body = { name: name };
    if (protocolId) { body.protocolId = parseInt(protocolId); }
    api("POST", "/api/plans", body).then(function() {
        document.getElementById("plan-name").value = "";
        loadPlans();
    });
}

function loadPlanDetail(planId) {
    currentPlanId = planId;
    api("GET", "/api/plans/" + planId).then(function(plan) {
        document.getElementById("plan-detail-name").textContent = plan.name;
        var statusEl = document.getElementById("plan-detail-status");
        statusEl.textContent = plan.status;
        statusEl.className = "status status-" + plan.status;
        document.getElementById("plan-detail-section").style.display = "block";
        document.getElementById("plan-tree").innerHTML = renderTree(plan.children, 0);
        loadMetrics(planId);
    });
}

// Change 4: Load and display metrics panel
function loadMetrics(planId) {
    api("GET", "/api/plans/" + planId + "/metrics").then(function(metrics) {
        var pct = Math.round(metrics.completionRatio * 100);
        document.getElementById("metrics-panel").innerHTML =
            '<div style="display:flex; gap:24px; flex-wrap:wrap;">' +
            '<div style="text-align:center;"><div style="font-size:28px; font-weight:700; color:#2e7d32;">' + pct + '%</div>' +
            '<div style="font-size:12px; color:#666;">Completion (' + metrics.completedLeaves + '/' + metrics.totalLeaves + ')</div></div>' +
            '<div style="text-align:center;"><div style="font-size:28px; font-weight:700; color:#1565c0;">$' + (metrics.totalCost || "0") + '</div>' +
            '<div style="font-size:12px; color:#666;">Resource Cost</div></div>' +
            '<div style="text-align:center;"><div style="font-size:28px; font-weight:700; color:' + (metrics.riskScore > 0 ? '#c62828' : '#666') + ';">' + metrics.riskScore + '</div>' +
            '<div style="font-size:12px; color:#666;">Risk Score</div></div>' +
            '</div>';
    }).catch(function() {
        document.getElementById("metrics-panel").innerHTML = "";
    });
}

function renderTree(children, depth) {
    if (!children || children.length === 0) {
        return '<div class="empty-state">No children</div>';
    }
    var result = "";
    for (var i = 0; i < children.length; i++) {
        var child = children[i];
        var isAction = child.type === "ACTION";
        var html = '<div class="tree-node ' + (depth === 0 ? "root" : "") + '">';
        if (!isAction && child.children) {
            html += '<span class="tree-toggle open" onclick="toggleTreeNode(this)">';
        }
        if (isAction) {
            html += '<span class="node-label" style="cursor:pointer;" onclick="loadActionDetail(' + child.id + ')">';
        } else {
            html += '<span class="node-label">';
        }
        html += child.name + "</span>";
        html += '<span class="node-type ' + (isAction ? "action" : "plan") + '">' + child.type + "</span>";
        html += '<span class="status status-' + child.status + '" style="margin-left:8px;">' + child.status + "</span>";
        if (!isAction && child.children) {
            html += "</span>";
            html += '<div class="tree-children">' + renderTree(child.children, depth + 1) + "</div>";
        }
        html += "</div>";
        result += html;
    }
    return result;
}

function toggleTreeNode(element) {
    element.classList.toggle("open");
    var childrenDiv = element.parentElement.querySelector(".tree-children");
    if (childrenDiv) {
        childrenDiv.style.display = element.classList.contains("open") ? "block" : "none";
    }
}

function loadActionDetail(actionId) {
    currentActionId = actionId;
    api("GET", "/api/actions/" + actionId).then(function(action) {
        document.getElementById("modal-action-name").textContent = action.name;

        var html = '<p><strong>State:</strong> <span class="status status-' + action.state + '">' + action.state + "</span></p>";
        html += "<p><strong>Party:</strong> " + (action.party || "-") + " | <strong>Location:</strong> " + (action.location || "-") + "</p>";

        // Change 1: new transition buttons
        html += '<div class="btn-group">';
        if (action.legalTransitions.indexOf("submitForApproval") !== -1) {
            html += '<button class="btn btn-primary btn-sm" onclick="doTransition(' + actionId + ", 'submit-for-approval'" + ')">Submit for Approval</button>';
        }
        if (action.legalTransitions.indexOf("approve") !== -1) {
            html += '<button class="btn btn-success btn-sm" onclick="openImplementModal(' + actionId + ", '" + (action.party || "") + "', '" + (action.location || "") + "'" + ')">Approve</button>';
        }
        if (action.legalTransitions.indexOf("reject") !== -1) {
            html += '<button class="btn btn-danger btn-sm" onclick="doTransition(' + actionId + ", 'reject'" + ')">Reject</button>';
        }
        if (action.legalTransitions.indexOf("complete") !== -1) {
            html += '<button class="btn btn-success btn-sm" onclick="doTransition(' + actionId + ", 'complete'" + ')">Complete</button>';
        }
        if (action.legalTransitions.indexOf("suspend") !== -1) {
            html += '<button class="btn btn-warning btn-sm" onclick="openSuspendModal(' + actionId + ')">Suspend</button>';
        }
        if (action.legalTransitions.indexOf("resume") !== -1) {
            html += '<button class="btn btn-primary btn-sm" onclick="doTransition(' + actionId + ", 'resume'" + ')">Resume</button>';
        }
        if (action.legalTransitions.indexOf("abandon") !== -1) {
            html += '<button class="btn btn-danger btn-sm" onclick="doTransition(' + actionId + ", 'abandon'" + ')">Abandon</button>';
        }
        if (action.legalTransitions.indexOf("reopen") !== -1) {
            html += '<button class="btn btn-warning btn-sm" onclick="doTransition(' + actionId + ", 'reopen'" + ')">Reopen</button>';
        }
        html += '<button class="btn btn-secondary btn-sm" onclick="openAllocModal(' + actionId + ')">+ Allocation</button>';
        html += "</div>";

        // F6: Allocations
        if (action.allocations.length > 0) {
            html += "<h3>Resource Allocations</h3>";
            html += "<table><thead><tr><th>Resource</th><th>Qty</th><th>Kind</th><th>Asset ID</th><th>Time Period</th></tr></thead><tbody>";
            for (var i = 0; i < action.allocations.length; i++) {
                var a = action.allocations[i];
                html += "<tr><td>" + a.resourceTypeName + "</td><td>" + a.quantity + "</td><td>" + a.kind + "</td><td>" + (a.assetId || "-") + "</td><td>" + (a.timePeriod || "-") + "</td></tr>";
            }
            html += "</tbody></table>";
        }

        // F5: Diff
        if (action.implementedAction) {
            var impl = action.implementedAction;
            html += "<h3>Implementation Details (Plan vs Reality)</h3>";
            html += "<table><thead><tr><th>Field</th><th>Proposed</th><th>Actual</th></tr></thead><tbody>";
            html += "<tr><td>Party</td><td>" + (action.party || "-") + "</td><td>" + (impl.actualParty || "-") + "</td></tr>";
            html += "<tr><td>Location</td><td>" + (action.location || "-") + "</td><td>" + (impl.actualLocation || "-") + "</td></tr>";
            html += "<tr><td>Start</td><td>-</td><td>" + (impl.actualStart || "-") + "</td></tr>";
            html += "<tr><td>Status</td><td>" + action.state + "</td><td>" + impl.status + "</td></tr>";
            html += "</tbody></table>";
            if (impl.diff && Object.keys(impl.diff).length > 0) {
                html += '<p style="color:#c62828; font-weight:600; margin-top:8px;">Differences detected:</p>';
                var diffKeys = Object.keys(impl.diff);
                for (var d = 0; d < diffKeys.length; d++) {
                    var field = diffKeys[d];
                    html += '<p style="margin-left:12px;">' + field + ': <span style="background:#ffebee; padding:2px 6px;">' + impl.diff[field].proposed + '</span> changed to <span style="background:#e8f5e9; padding:2px 6px;">' + impl.diff[field].actual + "</span></p>";
                }
            }
        }

        // Suspensions
        if (action.suspensions.length > 0) {
            html += "<h3>Suspensions</h3>";
            html += "<table><thead><tr><th>Reason</th><th>Start</th><th>End</th></tr></thead><tbody>";
            for (var i = 0; i < action.suspensions.length; i++) {
                var s = action.suspensions[i];
                html += "<tr><td>" + s.reason + "</td><td>" + s.startDate + "</td><td>" + (s.endDate || "Active") + "</td></tr>";
            }
            html += "</tbody></table>";
        }

        document.getElementById("modal-action-content").innerHTML = html;
        document.getElementById("action-modal").classList.add("show");
    });
}

function doTransition(actionId, event) {
    if (event === "suspend") { openSuspendModal(actionId); return; }
    api("POST", "/api/actions/" + actionId + "/" + event, {}).then(function() {
        closeModal("action-modal");
        if (currentPlanId) { loadPlanDetail(currentPlanId); }
    });
}

function openSuspendModal(actionId) {
    currentActionId = actionId;
    document.getElementById("suspend-reason").value = "";
    document.getElementById("suspend-modal").classList.add("show");
}

function confirmSuspend() {
    var reason = document.getElementById("suspend-reason").value || "No reason given";
    api("POST", "/api/actions/" + currentActionId + "/suspend", { reason: reason }).then(function() {
        closeModal("suspend-modal");
        closeModal("action-modal");
        if (currentPlanId) { loadPlanDetail(currentPlanId); }
    });
}

function openImplementModal(actionId, proposedParty, proposedLocation) {
    currentActionId = actionId;
    document.getElementById("impl-proposed-party").textContent = proposedParty || "-";
    document.getElementById("impl-proposed-location").textContent = proposedLocation || "-";
    document.getElementById("impl-actual-party").value = "";
    document.getElementById("impl-actual-location").value = "";
    document.getElementById("implement-modal").classList.add("show");
}

function confirmImplement() {
    var actualParty = document.getElementById("impl-actual-party").value;
    var actualLocation = document.getElementById("impl-actual-location").value;
    api("POST", "/api/actions/" + currentActionId + "/approve", {
        actualParty: actualParty || null,
        actualLocation: actualLocation || null
    }).then(function() {
        closeModal("implement-modal");
        closeModal("action-modal");
        if (currentPlanId) { loadPlanDetail(currentPlanId); }
    });
}

function openAllocModal(actionId) {
    currentActionId = actionId;
    document.getElementById("alloc-qty").value = "";
    document.getElementById("alloc-asset").value = "";
    document.getElementById("alloc-time").value = "";
    api("GET", "/api/resource-types").then(function(rts) {
        document.getElementById("alloc-rt").innerHTML =
            rts.map(function(rt) { return '<option value="' + rt.id + '">' + rt.name + " (" + rt.kind + ")</option>"; }).join("");
    });
    document.getElementById("alloc-modal").classList.add("show");
}

function confirmAllocation() {
    var resourceTypeId = parseInt(document.getElementById("alloc-rt").value);
    var quantity = parseFloat(document.getElementById("alloc-qty").value);
    var kind = document.getElementById("alloc-kind").value;
    var assetId = document.getElementById("alloc-asset").value || null;
    var timePeriod = document.getElementById("alloc-time").value || null;
    if (!quantity) { alert("Quantity required"); return; }
    api("POST", "/api/actions/" + currentActionId + "/allocations", {
        resourceTypeId: resourceTypeId, quantity: quantity, kind: kind, assetId: assetId, timePeriod: timePeriod
    }).then(function() {
        closeModal("alloc-modal");
        loadActionDetail(currentActionId);
    });
}

function openAddActionModal() {
    document.getElementById("new-action-name").value = "";
    document.getElementById("new-action-party").value = "";
    document.getElementById("new-action-location").value = "";
    document.getElementById("add-action-modal").classList.add("show");
}

function addActionToPlan() {
    var name = document.getElementById("new-action-name").value;
    if (!name) { alert("Action name required"); return; }
    api("POST", "/api/plans/" + currentPlanId + "/actions", {
        name: name,
        party: document.getElementById("new-action-party").value || null,
        location: document.getElementById("new-action-location").value || null
    }).then(function() {
        closeModal("add-action-modal");
        loadPlanDetail(currentPlanId);
    });
}

// Change 3: Report with status filter
function openReportModal() {
    var statusFilter = document.getElementById("report-status-filter") ?
        document.getElementById("report-status-filter").value : "";
    var url = "/api/plans/" + currentPlanId + "/report";
    if (statusFilter) { url += "?status=" + statusFilter; }

    api("GET", url).then(function(report) {
        var html = "<table><thead><tr><th>Type</th><th>Name</th><th>Status</th><th>Allocated Resources</th></tr></thead><tbody>";
        for (var i = 0; i < report.length; i++) {
            var node = report[i];
            var allocStr = "-";
            if (node.allocatedQuantities && Object.keys(node.allocatedQuantities).length > 0) {
                var parts = [];
                var keys = Object.keys(node.allocatedQuantities);
                for (var j = 0; j < keys.length; j++) { parts.push(keys[j] + ": " + node.allocatedQuantities[keys[j]]); }
                allocStr = parts.join(", ");
            }
            html += "<tr><td><span class='node-type " + (node.type === "ACTION" ? "action" : "plan") + "'>" + node.type + "</span></td>" +
                "<td>" + node.name + "</td>" +
                "<td><span class='status status-" + node.status + "'>" + node.status + "</span></td>" +
                "<td>" + allocStr + "</td></tr>";
        }
        html += "</tbody></table>";
        document.getElementById("report-content").innerHTML = html;
        document.getElementById("report-modal").classList.add("show");
    });
}

// Change 2: Ledger filter toggle
function loadLedger() {
    var entryFilter = document.getElementById("ledger-filter") ?
        document.getElementById("ledger-filter").value : "ALL";

    api("GET", "/api/accounts").then(function(accounts) {
        document.getElementById("accounts-table").innerHTML =
            accounts.map(function(a) {
                var safeName = a.name.replace(/'/g, "\\'");
                return "<tr><td>" + a.id + "</td><td>" + a.name + "</td><td>" + a.kind + "</td>" +
                    "<td>" + (a.resourceTypeName || "-") + "</td>" +
                    '<td class="' + (parseFloat(a.balance) < 0 ? "balance-alert" : "balance-ok") + '">' + a.balance + "</td>" +
                    "<td><button class='btn btn-secondary btn-sm' onclick=\"loadEntries(" + a.id + ", '" + safeName + "')\">Entries</button></td></tr>";
            }).join("") || '<tr><td colspan="6" class="empty-state">No accounts</td></tr>';

        // Posting rule dropdowns
        var poolAccounts = accounts.filter(function(a) { return a.kind === "POOL"; });
        var alertAccounts = accounts.filter(function(a) { return a.kind === "ALERT_MEMO"; });
        var triggerSelect = document.getElementById("rule-trigger");
        var outputSelect = document.getElementById("rule-output");
        if (triggerSelect) {
            triggerSelect.innerHTML = poolAccounts.map(function(a) {
                return '<option value="' + a.id + '">' + a.name + "</option>";
            }).join("");
        }
        if (outputSelect) {
            outputSelect.innerHTML = alertAccounts.map(function(a) {
                return '<option value="' + a.id + '">' + a.name + "</option>";
            }).join("");
        }

        return api("GET", "/api/posting-rules");
    }).then(function(rules) {
        document.getElementById("posting-rules-table").innerHTML =
            rules.map(function(r) {
                return "<tr><td>" + r.triggerAccountName + "</td><td>" + r.outputAccountName + "</td><td>" + r.strategyType + "</td></tr>";
            }).join("") || '<tr><td colspan="3" class="empty-state">No posting rules</td></tr>';
    });
}

function createPostingRule() {
    var triggerAccountId = parseInt(document.getElementById("rule-trigger").value);
    var outputAccountId = parseInt(document.getElementById("rule-output").value);
    if (!triggerAccountId || !outputAccountId) { alert("Select both accounts"); return; }
    api("POST", "/api/posting-rules", {
        triggerAccountId: triggerAccountId,
        outputAccountId: outputAccountId,
        strategyType: "OVER_CONSUMPTION_ALERT"
    }).then(function() { loadLedger(); });
}

function loadEntries(accountId, accountName) {
    var filter = document.getElementById("ledger-filter") ? document.getElementById("ledger-filter").value : "ALL";
    api("GET", "/api/accounts/" + accountId + "/entries").then(function(entries) {
        // Change 2: filter entries by type
        var filtered = entries;
        if (filter === "CONSUMABLE") {
            filtered = entries.filter(function(e) { return e.transactionDescription && e.transactionDescription.indexOf("asset") === -1; });
        } else if (filter === "ASSET") {
            filtered = entries.filter(function(e) { return e.transactionDescription && e.transactionDescription.indexOf("asset") !== -1; });
        }

        document.getElementById("entries-account-name").textContent = accountName;
        document.getElementById("entries-table").innerHTML =
            filtered.map(function(e) {
                return "<tr><td>" + e.id + '</td><td class="' + (parseFloat(e.amount) < 0 ? "balance-alert" : "") + '">' + e.amount + "</td>" +
                    "<td>" + (e.chargedAt || "-") + "</td>" +
                    "<td>" + (e.bookedAt || "-") + "</td>" +
                    "<td>" + (e.actionId || "-") + "</td>" +
                    "<td>" + (e.transactionDescription || "-") + "</td></tr>";
            }).join("") || '<tr><td colspan="6" class="empty-state">No entries</td></tr>';
        document.getElementById("entries-section").style.display = "block";
    });
}

function loadAudit() {
    api("GET", "/api/audit-log").then(function(entries) {
        document.getElementById("audit-table").innerHTML =
            entries.map(function(e) {
                return "<tr><td>" + e.id + "</td><td>" + e.event + "</td><td>" + (e.actionId || "-") + "</td>" +
                    "<td>" + (e.accountId || "-") + "</td><td>" + (e.entryId || "-") + "</td><td>" + e.timestamp + "</td></tr>";
            }).join("") || '<tr><td colspan="6" class="empty-state">No audit entries</td></tr>';
    });
}

function closeModal(id) {
    document.getElementById(id).classList.remove("show");
}

var overlays = document.querySelectorAll(".modal-overlay");
for (var i = 0; i < overlays.length; i++) {
    overlays[i].addEventListener("click", function(e) {
        if (e.target === this) { this.classList.remove("show"); }
    });
}

loadDashboard();