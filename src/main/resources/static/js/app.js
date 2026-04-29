var API = "https://resourceplanningledger.onrender.com";
var currentPlanId = null;
var currentActionId = null;
var stepCount = 0;

// ================= Navigation =================
function showPage(page, tabElement) {
    document.querySelectorAll(".page").forEach(p => p.classList.remove("active"));
    document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
    document.getElementById("page-" + page).classList.add("active");
    tabElement.classList.add("active");
    loadPageData(page);
}

function loadPageData(page) {
    if (page === "dashboard") loadDashboard();
    if (page === "protocols") loadProtocols();
    if (page === "plans") loadPlans();
    if (page === "ledger") loadLedger();
    if (page === "audit") loadAudit();
}

// ================= API =================
function api(method, path, body) {
    var opts = { method: method, headers: { "Content-Type": "application/json" } };
    if (body) opts.body = JSON.stringify(body);
    return fetch(API + path, opts).then(res => {
        if (!res.ok) {
            return res.json().catch(() => ({})).then(err => {
                alert(err.message || "Error");
                throw new Error(err.message);
            });
        }
        return res.status === 204 ? null : res.json();
    });
}

// ================= Plans =================
function loadPlans() {
    loadProtocols();
    api("GET", "/api/plans").then(plans => {
        document.getElementById("plans-list").innerHTML =
            plans.map(p =>
                `<div class="plan-item" onclick="loadPlanDetail(${p.id})">
                    <strong>${p.name}</strong>
                    <span class="status status-${p.status}">${p.status}</span>
                </div>`
            ).join("") || '<div>No plans</div>';
    });
}

function loadPlanDetail(planId) {
    currentPlanId = planId;
    api("GET", "/api/plans/" + planId).then(plan => {
        document.getElementById("plan-detail-name").textContent = plan.name;
        document.getElementById("plan-tree").innerHTML = renderTree(plan.children);
    });
}

// ================= Tree =================
function renderTree(children) {
    if (!children || children.length === 0) return "No children";

    return children.map(child => {
        if (child.type === "ACTION") {
            return `<div onclick="loadActionDetail(${child.id})">${child.name}</div>`;
        }
        return `<div>${child.name}${renderTree(child.children)}</div>`;
    }).join("");
}

// ================= ACTION DETAIL =================
function loadActionDetail(actionId) {
    currentActionId = actionId;

    api("GET", "/api/actions/" + actionId).then(action => {
        document.getElementById("modal-action-name").textContent = action.name;

        var html = `
            <p><b>State:</b> ${action.state}</p>
            <p><b>Party:</b> ${action.party || "-"} | <b>Location:</b> ${action.location || "-"}</p>
        `;

        // ===== Buttons =====
        html += `<div class="btn-group">`;

        if (action.legalTransitions.includes("implement")) {
            html += `<button onclick="openImplementModal(${action.id}, '${action.party || ""}', '${action.location || ""}')">Implement</button>`;
        }

        if (action.legalTransitions.includes("complete")) {
            html += `<button onclick="doTransition(${action.id}, 'complete')">Complete</button>`;
        }

        if (action.legalTransitions.includes("resume")) {
            html += `<button onclick="doTransition(${action.id}, 'resume')">Resume</button>`;
        }

        if (action.legalTransitions.includes("abandon")) {
            html += `<button onclick="doTransition(${action.id}, 'abandon')">Abandon</button>`;
        }

        html += `</div>`;

        // ===== F5 =====
        if (action.implementedAction) {
            var impl = action.implementedAction;

            html += `
                <h3>Plan vs Reality</h3>
                <table>
                    <tr><td>Party</td><td>${action.party || "-"}</td><td>${impl.actualParty || "-"}</td></tr>
                    <tr><td>Location</td><td>${action.location || "-"}</td><td>${impl.actualLocation || "-"}</td></tr>
                    <tr><td>Start</td><td>-</td><td>${impl.actualStart}</td></tr>
                </table>
            `;
        }

        document.getElementById("modal-action-content").innerHTML = html;
        document.getElementById("action-modal").classList.add("show");
    });
}

// ================= IMPLEMENT MODAL =================
function openImplementModal(actionId, party, location) {
    currentActionId = actionId;

    document.getElementById("impl-proposed-party").textContent = party || "-";
    document.getElementById("impl-proposed-location").textContent = location || "-";

    document.getElementById("impl-actual-party").value = "";
    document.getElementById("impl-actual-location").value = "";

    document.getElementById("implement-modal").classList.add("show");
}

function confirmImplement() {
    var actualParty = document.getElementById("impl-actual-party").value;
    var actualLocation = document.getElementById("impl-actual-location").value;

    api("POST", "/api/actions/" + currentActionId + "/implement", {
        actualParty,
        actualLocation
    }).then(() => {
        closeModal("implement-modal");
        loadPlanDetail(currentPlanId);
    });
}

// ================= TRANSITIONS =================
function doTransition(actionId, event) {
    api("POST", "/api/actions/" + actionId + "/" + event)
        .then(() => loadPlanDetail(currentPlanId));
}

// ================= MODALS =================
function closeModal(id) {
    document.getElementById(id).classList.remove("show");
}

document.querySelectorAll(".modal-overlay").forEach(el => {
    el.addEventListener("click", function(e) {
        if (e.target === this) this.classList.remove("show");
    });
});
function loadDashboard() {
    console.log("Dashboard loaded");
}

// ================= INIT =================
loadDashboard();