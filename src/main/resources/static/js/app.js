const API = '';
let currentPlanId = null;
let currentActionId = null;
let stepCount = 0;

// =============================================================================
// Navigation
// =============================================================================
function showPage(page, tabElement) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.getElementById('page-' + page).classList.add('active');
    tabElement.classList.add('active');
    loadPageData(page);
}

function loadPageData(page) {
    switch (page) {
        case 'dashboard': loadDashboard(); break;
        case 'protocols': loadProtocols(); break;
        case 'plans':     loadPlans();     break;
        case 'ledger':    loadLedger();    break;
        case 'audit':     loadAudit();     break;
    }
}

// =============================================================================
// API Helper
// =============================================================================
async function api(method, path, body) {
    const opts = { method, headers: { 'Content-Type': 'application/json' } };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(API + path, opts);
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        alert(err.message || 'Error');
        throw new Error(err.message);
    }
    if (res.status === 204) return null;
    return res.json();
}

// =============================================================================
// Dashboard
// =============================================================================
async function loadDashboard() {
    const accounts = await api('GET', '/api/accounts');
    const poolAccounts = accounts.filter(a => a.kind === 'POOL');

    // Alert section
    const alertSection = document.getElementById('alert-section');
    const belowZero = poolAccounts.filter(a => parseFloat(a.balance) < 0);
    if (belowZero.length > 0) {
        alertSection.innerHTML =
            '<div class="alert-banner"><strong>⚠ Over-consumption Alert</strong>' +
            belowZero.map(a => a.name + ': ' + a.balance).join(', ') +
            '</div>';
    } else {
        alertSection.innerHTML = '';
    }

    // Pool balances table
    document.getElementById('pool-balances').innerHTML =
        poolAccounts.map(a =>
            '<tr>' +
                '<td>' + a.name + '</td>' +
                '<td>' + (a.resourceTypeName || '-') + '</td>' +
                '<td class="' + (parseFloat(a.balance) < 0 ? 'balance-alert' : 'balance-ok') + '">' + a.balance + '</td>' +
            '</tr>'
        ).join('') || '<tr><td colspan="3" class="empty-state">No pool accounts yet</td></tr>';

    // Plans overview table
    const plans = await api('GET', '/api/plans');
    document.getElementById('plans-overview').innerHTML =
        plans.map(p =>
            '<tr>' +
                '<td>' + p.name + '</td>' +
                '<td><span class="status status-' + p.status + '">' + p.status + '</span></td>' +
                '<td>' + p.actionCount + ' actions, ' + p.subPlanCount + ' sub-plans</td>' +
            '</tr>'
        ).join('') || '<tr><td colspan="3" class="empty-state">No plans yet</td></tr>';

    // Resource types table
    const rts = await api('GET', '/api/resource-types');
    document.getElementById('resource-types-table').innerHTML =
        rts.map(rt =>
            '<tr>' +
                '<td>' + rt.id + '</td>' +
                '<td>' + rt.name + '</td>' +
                '<td>' + rt.kind + '</td>' +
                '<td>' + rt.unit + '</td>' +
                '<td>' + (rt.poolAccountId || '-') + '</td>' +
            '</tr>'
        ).join('') || '<tr><td colspan="5" class="empty-state">No resource types</td></tr>';
}

async function createResourceType() {
    const name = document.getElementById('rt-name').value;
    const kind = document.getElementById('rt-kind').value;
    const unit = document.getElementById('rt-unit').value;
    if (!name || !unit) { alert('Name and unit required'); return; }
    await api('POST', '/api/resource-types', { name, kind, unit });
    document.getElementById('rt-name').value = '';
    document.getElementById('rt-unit').value = '';
    loadDashboard();
}

// =============================================================================
// Protocols
// =============================================================================
function addStepField() {
    stepCount++;
    const div = document.createElement('div');
    div.className = 'form-row';
    div.style.marginBottom = '6px';
    div.innerHTML =
        '<div class="form-group">' +
            '<input placeholder="Step name" id="step-name-' + stepCount + '">' +
        '</div>' +
        '<div class="form-group">' +
            '<input placeholder="Depends on (comma-sep)" id="step-deps-' + stepCount + '">' +
        '</div>';
    document.getElementById('steps-container').appendChild(div);
}

async function createProtocol() {
    const name = document.getElementById('proto-name').value;
    const desc = document.getElementById('proto-desc').value;
    if (!name) { alert('Protocol name required'); return; }

    const steps = [];
    for (let i = 1; i <= stepCount; i++) {
        const sName = document.getElementById('step-name-' + i);
        if (sName && sName.value) {
            const deps = document.getElementById('step-deps-' + i).value;
            steps.push({
                name: sName.value,
                dependsOn: deps ? deps.split(',').map(s => s.trim()).filter(s => s) : []
            });
        }
    }

    await api('POST', '/api/protocols', { name, description: desc, steps });
    document.getElementById('proto-name').value = '';
    document.getElementById('proto-desc').value = '';
    document.getElementById('steps-container').innerHTML = '';
    stepCount = 0;
    loadProtocols();
}

async function loadProtocols() {
    const protocols = await api('GET', '/api/protocols');
    document.getElementById('protocols-list').innerHTML =
        protocols.map(p =>
            '<div style="border:1px solid #eee; padding:12px; margin-bottom:8px; border-radius:4px;">' +
                '<strong>' + p.name + '</strong> <span style="color:#999;">(ID: ' + p.id + ')</span>' +
                '<p style="color:#666; margin:4px 0;">' + (p.description || 'No description') + '</p>' +
                '<div style="font-size:13px; color:#555;">Steps: ' +
                    (p.steps.map(s =>
                        s.name + (s.dependsOn && s.dependsOn.length ? ' → depends on: ' + s.dependsOn.join(', ') : '')
                    ).join(' | ') || 'none') +
                '</div>' +
            '</div>'
        ).join('') || '<div class="empty-state">No protocols yet</div>';

    // Update plan creation dropdown
    const select = document.getElementById('plan-protocol');
    if (select) {
        select.innerHTML = '<option value="">— From scratch —</option>' +
            protocols.map(p => '<option value="' + p.id + '">' + p.name + '</option>').join('');
    }
}

// =============================================================================
// Plans
// =============================================================================
async function loadPlans() {
    await loadProtocols();
    const plans = await api('GET', '/api/plans');
    document.getElementById('plans-list').innerHTML =
        plans.map(p =>
            '<div style="border:1px solid #eee; padding:12px; margin-bottom:8px; border-radius:4px; cursor:pointer;" ' +
                'onclick="loadPlanDetail(' + p.id + ')">' +
                '<strong>' + p.name + '</strong>' +
                '<span class="status status-' + p.status + '" style="margin-left:8px;">' + p.status + '</span>' +
                '<span style="color:#999; margin-left:8px;">' + p.actionCount + ' actions</span>' +
            '</div>'
        ).join('') || '<div class="empty-state">No plans yet</div>';
}

async function createPlan() {
    const name = document.getElementById('plan-name').value;
    const protocolId = document.getElementById('plan-protocol').value;
    if (!name) { alert('Plan name required'); return; }
    const body = { name };
    if (protocolId) body.protocolId = parseInt(protocolId);
    await api('POST', '/api/plans', body);
    document.getElementById('plan-name').value = '';
    loadPlans();
}

async function loadPlanDetail(planId) {
    currentPlanId = planId;
    const plan = await api('GET', '/api/plans/' + planId);
    document.getElementById('plan-detail-name').textContent = plan.name;
    const statusEl = document.getElementById('plan-detail-status');
    statusEl.textContent = plan.status;
    statusEl.className = 'status status-' + plan.status;
    document.getElementById('plan-detail-section').style.display = 'block';
    document.getElementById('plan-tree').innerHTML = renderTree(plan.children, 0);
}

function renderTree(children, depth) {
    if (!children || children.length === 0) {
        return '<div class="empty-state">No children</div>';
    }

    return children.map(function(child) {
        var isAction = child.type === 'ACTION';
        var statusClass = 'status-' + child.status;
        var html = '<div class="tree-node ' + (depth === 0 ? 'root' : '') + '">';

        if (!isAction && child.children) {
            html += '<span class="tree-toggle open" onclick="toggleTreeNode(this)">';
        }

        if (isAction) {
            html += '<span class="node-label" style="cursor:pointer;" onclick="loadActionDetail(' + child.id + ')">';
        } else {
            html += '<span class="node-label">';
        }
        html += child.name + '</span>';
        html += '<span class="node-type ' + (isAction ? 'action' : 'plan') + '">' + child.type + '</span>';
        html += '<span class="status ' + statusClass + '" style="margin-left:8px;">' + child.status + '</span>';

        if (!isAction && child.children) {
            html += '</span>';
            html += '<div class="tree-children">' + renderTree(child.children, depth + 1) + '</div>';
        }

        html += '</div>';
        return html;
    }).join('');
}

function toggleTreeNode(element) {
    element.classList.toggle('open');
    var childrenDiv = element.parentElement.querySelector('.tree-children');
    if (childrenDiv) {
        childrenDiv.style.display = element.classList.contains('open') ? 'block' : 'none';
    }
}

// =============================================================================
// Action Detail
// =============================================================================
async function loadActionDetail(actionId) {
    currentActionId = actionId;
    var action = await api('GET', '/api/actions/' + actionId);

    document.getElementById('modal-action-name').textContent = action.name;

    var html = '<p><strong>State:</strong> <span class="status status-' + action.state + '">' + action.state + '</span></p>';
    html += '<p><strong>Party:</strong> ' + (action.party || '-') + ' | <strong>Location:</strong> ' + (action.location || '-') + '</p>';

    // Transition buttons
    html += '<div class="btn-group">';
    if (action.legalTransitions.indexOf('implement') !== -1) {
        html += '<button class="btn btn-pri