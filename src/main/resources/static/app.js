const API_BASE = "/api";

function qs(sel) { return document.querySelector(sel); }
function qsa(sel) { return Array.from(document.querySelectorAll(sel)); }

function toast(title, message, kind = "info") {
  const container = qs("#toastContainer");
  const el = document.createElement("div");
  el.className = "toast";
  el.innerHTML = `
    <div class="t-title">${escapeHtml(title)}</div>
    <div class="t-msg">${escapeHtml(message || "")}</div>
  `;
  if (kind === "error") el.style.borderColor = "rgba(251,113,133,.55)";
  if (kind === "success") el.style.borderColor = "rgba(52,211,153,.45)";
  container.appendChild(el);
  setTimeout(() => el.remove(), 4200);
}

function escapeHtml(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function setLoading(btn, isLoading) {
  if (!btn) return;
  btn.disabled = isLoading;
  const icon = btn.querySelector("i");
  if (isLoading && icon) icon.className = "fa-solid fa-spinner fa-spin";
  if (!isLoading && icon) {
    // Keep whatever original icon was; fallback
    if (icon.className.includes("fa-spin")) icon.className = "fa-solid fa-paper-plane";
  }
}

function statusOrder() {
  return ["SUBMITTED", "TRIAGED", "IN_PROGRESS", "RESOLVED"];
}

function renderTimeline(container, status) {
  const order = statusOrder();
  const activeIdx = Math.max(0, order.indexOf(status));
  container.innerHTML = "";

  const steps = [
    { key: "SUBMITTED", name: "Submitted", sub: "Case created + auto triage" },
    { key: "TRIAGED", name: "Triaged", sub: "Category + priority set" },
    { key: "IN_PROGRESS", name: "In Progress", sub: "Agent working on it" },
    { key: "RESOLVED", name: "Resolved", sub: "Resolution shared" }
  ];

  for (let i = 0; i < steps.length; i++) {
    const s = steps[i];
    const cls = i < activeIdx ? "done" : i === activeIdx ? "active" : "";
    const node = document.createElement("div");
    node.className = "timeline-step " + cls;
    node.innerHTML = `
      <div class="timeline-dot"></div>
      <div>
        <div class="tname">${escapeHtml(s.name)}</div>
        <div class="tsub">${escapeHtml(s.sub)}</div>
      </div>
    `;
    container.appendChild(node);
  }
}

function priorityStars(priority) {
  const p = Number(priority || 1);
  const max = 5;
  const count = Math.min(max, Math.max(1, p));
  return Array.from({ length: max }, (_, i) => i < count ? "on" : "").map((cls, idx) => {
    return `<span class="star ${cls}" title="Priority level ${count}">${""}</span>`;
  }).join("");
}

function badgeForPriority(priority) {
  const p = Number(priority || 1);
  if (p >= 5) return { cls: "bad", label: "Urgent" };
  if (p >= 4) return { cls: "bad", label: "High" };
  if (p >= 3) return { cls: "warn", label: "Medium" };
  return { cls: "good", label: "Low" };
}

function slaDeadlineHours(type) {
  return type === "COMPLAINT" ? 48 : 24;
}

function hoursBetween(iso) {
  const t = new Date(iso).getTime();
  const now = Date.now();
  return Math.max(0, (now - t) / 36e5);
}

function renderCaseCard(c) {
  const badge = badgeForPriority(c.priority);
  const pBadge = `
    <span class="badge ${badge.cls}">
      <span class="dot"></span>${escapeHtml(badge.label)} Priority (${c.priority}/5)
    </span>
  `;

  const statusBadge = `
    <span class="badge">
      <span class="dot"></span>${escapeHtml(c.status)}
    </span>
  `;

  const resolved = c.status === "RESOLVED";
  const resolutionHtml = resolved
    ? `<div class="resolution">
         <div class="m-title"><i class="fa-solid fa-circle-check"></i> Resolution</div>
         <div class="res-body">${escapeHtml(c.resolutionText || "Resolution provided.")}</div>
       </div>`
    : `<div class="resolution" style="border-color: rgba(96,165,250,.22); background: rgba(96,165,250,.07);">
         <div class="m-title" style="color: rgba(96,165,250,.95);"><i class="fa-solid fa-hourglass-half"></i> Next Update</div>
         <div class="res-body">${escapeHtml(c.triageSuggestion || "We’re working on this case. Check timeline for progress.")}</div>
       </div>`;

  const ageH = c.createdAt ? hoursBetween(c.createdAt) : 0;
  const slaH = slaDeadlineHours(c.type);
  const progress = resolved ? 100 : Math.round(Math.min(100, (ageH / slaH) * 100));
  const overdue = !resolved && ageH > slaH;

  const slaHtml = `
    <div class="meta" style="display:flex; gap:12px; align-items:center; margin-top: 10px;">
      <span><strong>SLA</strong>: ${c.type === "COMPLAINT" ? "48h" : "24h"}</span>
      <span><strong>Age</strong>: ${ageH.toFixed(1)}h</span>
      <span style="margin-left:auto; color:${overdue ? "rgba(251,113,133,.98)" : "var(--muted)"};">
        ${overdue ? "<strong>Overdue</strong>" : `Progress: ${progress}%`}
      </span>
    </div>
    <div class="sla-bar" aria-hidden="true">
      <div class="sla-bar-fill" style="width:${progress}%; background:${overdue ? "linear-gradient(90deg, rgba(251,113,133,.95), rgba(167,139,250,.65))" : "linear-gradient(90deg, rgba(110,231,255,.9), rgba(167,139,250,.75))"};"></div>
    </div>
  `;

  const node = document.createElement("div");
  node.className = "case-card";
  node.innerHTML = `
    <div class="top">
      <div>
        <div class="ticket">${escapeHtml(c.ticketNumber)}</div>
        <div class="meta">
          <strong>${escapeHtml(c.type === "COMPLAINT" ? "Complaint" : "Feedback")}</strong>
          • ${escapeHtml(c.category || "Auto-categorizing...")}
        </div>
        ${slaHtml}
      </div>
      <div style="display:flex; flex-direction:column; gap:10px; align-items:flex-end;">
        ${statusBadge}
        ${pBadge}
      </div>
    </div>

    <div class="case-grid">
      <div>
        <div class="meta"><strong>Customer</strong>: ${escapeHtml(c.customerName)} (${escapeHtml(c.customerEmail)})</div>
        <div class="meta" style="margin-top:8px;"><strong>Flight</strong>: ${escapeHtml(c.flightNumber || "-")} • ${escapeHtml(c.journeyDate || "-")}</div>
        <div class="meta" style="margin-top:8px;"><strong>Channel</strong>: ${escapeHtml(c.contactChannel || "-")}</div>
        <div class="meta" style="margin-top:8px;">
          <strong>Priority</strong>: ${priorityStars(c.priority)}
        </div>
      </div>
      <div class="mini-timeline">
        <div class="meta" style="margin-bottom: 6px;"><strong>Timeline</strong></div>
        <div class="timeline" id="timeline-${escapeHtml(c.id)}"></div>
      </div>
    </div>

    <div class="message">
      <div class="m-title"><i class="fa-solid fa-message"></i> Message</div>
      <div>${escapeHtml(c.message || "")}</div>
    </div>

    ${resolutionHtml}
    <div class="meta" style="margin-top: 10px;">
      <strong>Assigned</strong>: ${escapeHtml(c.assignedAgent || "Unassigned")}
      • <strong>Created</strong>: ${escapeHtml(c.createdAt ? new Date(c.createdAt).toLocaleString() : "-")}
    </div>
  `;

  // Render timeline after node is in DOM
  setTimeout(() => renderTimeline(node.querySelector(`#timeline-${c.id}`), c.status), 0);
  return node;
}

function readForm(form) {
  const data = {};
  for (const input of form.querySelectorAll("[name]")) {
    if (input.type === "checkbox") continue;
    data[input.name] = input.value;
  }
  return data;
}

function setTab(tabId) {
  qsa(".tab").forEach(btn => {
    const is = btn.dataset.tab === tabId;
    btn.classList.toggle("active", is);
    btn.setAttribute("aria-selected", is ? "true" : "false");
  });

  qsa(".tab-panel").forEach(panel => {
    panel.classList.add("hidden");
    if (panel.id === tabId) panel.classList.remove("hidden");
  });

  // Remember last active tab so refresh keeps the same screen.
  try {
    localStorage.setItem("airline-active-tab", tabId);
  } catch {
    // ignore
  }
}

function connectTabs() {
  qsa(".tab").forEach(btn => {
    btn.addEventListener("click", () => {
      const tabId = btn.dataset.tab;
      setTab(tabId);
      if (tabId === "analyticsTab") loadAnalytics(true);
      if (tabId === "analyticsTab") loadKpis();
    });
  });
}

function formatDateTime(iso) {
  try {
    if (!iso) return "-";
    return new Date(iso).toLocaleString();
  } catch {
    return "-";
  }
}

let analyticsLoadedOnce = false;

async function loadAnalytics(force = false) {
  if (analyticsLoadedOnce && !force) return;

  const summaryResEl = qs("#analyticsTotal");
  const complaintsFullList = qs("#analyticsComplaintsFullList");
  const feedbackFullList = qs("#analyticsFeedbackFullList");
  if (!summaryResEl || !complaintsFullList || !feedbackFullList) return;

  try {
    const sRes = await fetch(`${API_BASE}/analytics/summary`);
    const summary = await sRes.json();
    if (!sRes.ok) throw new Error(summary.message || "Analytics summary failed");

    qs("#analyticsTotal").textContent = summary.totalCases ?? 0;
    qs("#analyticsFeedback").textContent = summary.feedbackCount ?? 0;
    qs("#analyticsComplaints").textContent = summary.complaintCount ?? 0;
    qs("#analyticsResolved").textContent = summary.resolvedCount ?? 0;
    qs("#analyticsOpen").textContent = summary.openCount ?? 0;
    qs("#analyticsHigh").textContent = summary.highPriorityCount ?? 0;
    qs("#analyticsLast7").textContent = summary.last7DaysCount ?? 0;

    const rRes = await fetch(`${API_BASE}/analytics/recent?limit=20`);
    const recent = await rRes.json();
    if (!rRes.ok) throw new Error(recent.message || "Analytics recent failed");

    const complaints = (recent || []).filter(x => x.type === "COMPLAINT");
    const feedback = (recent || []).filter(x => x.type === "FEEDBACK");
    complaints.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));

    complaintsFullList.innerHTML = "";
    if (!complaints.length) {
      complaintsFullList.innerHTML = `<div class="complaint-item"><div class="meta">No complaints yet.</div></div>`;
    } else {
      for (const c of complaints) {
        const item = document.createElement("div");
        item.className = "complaint-item";
        item.innerHTML = `
          <div class="top">
            <div>
              <div class="ticket">${escapeHtml(c.ticketNumber || "")}</div>
              <div class="meta">${escapeHtml(c.customerEmail || "")} • ${escapeHtml(c.status || "")}</div>
            </div>
            <div class="meta">${escapeHtml(formatDateTime(c.createdAt))}</div>
          </div>
          <div class="msg">${escapeHtml(c.message || "")}</div>
          <div style="margin-top:10px; display:flex; justify-content:flex-end;">
            <button class="btn btn-danger btn-mini" type="button" data-delete-id="${escapeHtml(c.id)}">
              <i class="fa-solid fa-trash"></i>
              Delete
            </button>
          </div>
        `;
        complaintsFullList.appendChild(item);
      }
    }

    const feedbackSorted = [...feedback].sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
    feedbackFullList.innerHTML = "";
    if (!feedbackSorted.length) {
      feedbackFullList.innerHTML = `<div class="complaint-item"><div class="meta">No feedback yet.</div></div>`;
    } else {
      for (const c of feedbackSorted) {
        const item = document.createElement("div");
        item.className = "complaint-item";
        item.innerHTML = `
          <div class="top">
            <div>
              <div class="ticket">${escapeHtml(c.ticketNumber || "")}</div>
              <div class="meta">${escapeHtml(c.customerEmail || "")} • ${escapeHtml(c.status || "")}</div>
            </div>
            <div class="meta">${escapeHtml(formatDateTime(c.createdAt))}</div>
          </div>
          <div class="msg">${escapeHtml(c.message || "")}</div>
          <div style="margin-top:10px; display:flex; justify-content:flex-end;">
            <button class="btn btn-danger btn-mini" type="button" data-delete-id="${escapeHtml(c.id)}">
              <i class="fa-solid fa-trash"></i>
              Delete
            </button>
          </div>
        `;
        feedbackFullList.appendChild(item);
      }
    }

    analyticsLoadedOnce = true;
  } catch (e) {
    toast("Analytics failed", e.message || String(e), "error");
  }
}

async function handleCreateCase(form, endpoint) {
  const btn = form.querySelector('button[type="submit"]');
  setLoading(btn, true);
  try {
    const payload = readForm(form);
    const res = await fetch(API_BASE + endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || "Request failed");

    toast("Case created", `Ticket: ${data.ticketNumber}`, "success");
    renderCaseResults([data], true);
    setTab("trackTab");

    // Ensure Analytics shows updated counts after submission.
    // We keep a "loaded once" optimization, but must invalidate it after mutations.
    analyticsLoadedOnce = false;
    loadKpis();

    // If user happens to be on Analytics tab already, refresh immediately.
    const analyticsPanel = qs("#analyticsTab");
    if (analyticsPanel && !analyticsPanel.classList.contains("hidden")) {
      loadAnalytics(true);
    }
  } catch (e) {
    toast("Submission failed", e.message || String(e), "error");
  } finally {
    setLoading(btn, false);
  }
}

let lastRenderedCases = [];

function renderCaseResults(cases, enableCsv = false) {
  const out = qs("#caseResults");
  out.innerHTML = "";
  lastRenderedCases = cases || [];
  if (!cases || cases.length === 0) {
    out.innerHTML = `<div class="case-card"><div class="meta"><strong>No cases found.</strong></div></div>`;
    if (enableCsv) qs("#btnExportCsv").disabled = true;
    return;
  }
  for (const c of cases) out.appendChild(renderCaseCard(c));
  if (enableCsv) qs("#btnExportCsv").disabled = false;
}

async function lookupByTicket(ticketNumber) {
  const res = await fetch(`${API_BASE}/cases/lookup?ticketNumber=${encodeURIComponent(ticketNumber)}`);
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || "Lookup failed");
  return data;
}

async function lookupByEmail(email, limit) {
  const res = await fetch(`${API_BASE}/cases/my?email=${encodeURIComponent(email)}&limit=${encodeURIComponent(limit)}`);
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || "Lookup failed");
  return data;
}

function trackToggle() {
  const ticketArea = qs("#trackTicketArea");
  const emailArea = qs("#trackEmailArea");
  qsa("input[name='trackMode']").forEach(r => {
    r.addEventListener("change", () => {
      const mode = qsa("input[name='trackMode']").find(x => x.checked).value;
      ticketArea.classList.toggle("hidden", mode !== "ticket");
      emailArea.classList.toggle("hidden", mode !== "email");
    });
  });
}

function toCsv(cases) {
  const headers = [
    "ticketNumber",
    "type",
    "status",
    "category",
    "priority",
    "triageSuggestion",
    "customerName",
    "customerEmail",
    "flightNumber",
    "journeyDate",
    "contactChannel",
    "sentimentScore",
    "assignedAgent",
    "createdAt",
    "resolvedAt",
    "resolutionText"
  ];
  const escape = (v) => {
    const s = v == null ? "" : String(v);
    const withQuotes = s.includes(",") || s.includes('"') || s.includes("\n");
    const val = s.replaceAll('"', '""');
    return withQuotes ? `"${val}"` : val;
  };
  const rows = cases.map(c => headers.map(h => escape(c[h])).join(","));
  return [headers.join(","), ...rows].join("\n");
}

function downloadText(filename, text, mime) {
  const blob = new Blob([text], { type: mime || "text/plain;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function connectTrack() {
  qs("#btnLookup").addEventListener("click", async () => {
    const ticketNumber = qs("#trackTicketNumber").value.trim();
    if (!ticketNumber) return toast("Missing ticket", "Enter a ticket number.", "error");
    try {
      toast("Loading case...", ticketNumber);
      const c = await lookupByTicket(ticketNumber);
      renderCaseResults([c], true);
    } catch (e) {
      toast("Lookup failed", e.message || String(e), "error");
    }
  });

  qs("#btnLookupEmail").addEventListener("click", async () => {
    const email = qs("#trackEmail").value.trim();
    const limit = Number(qs("#trackLimit").value || 10);
    if (!email) return toast("Missing email", "Enter an email.", "error");
    try {
      const cases = await lookupByEmail(email, limit);
      renderCaseResults(cases, true);
    } catch (e) {
      toast("Lookup failed", e.message || String(e), "error");
    }
  });

  qs("#btnExportCsv").addEventListener("click", () => {
    const cases = lastRenderedCases || [];
    if (!cases.length) return;
    const csv = toCsv(cases);
    downloadText("airline-cases-export.csv", csv, "text/csv;charset=utf-8");
    toast("Export ready", "Downloaded CSV.", "success");
  });
}

function connectAdmin() {
  const form = qs("#adminForm");
  const preview = qs("#adminPreview");

  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    preview.innerHTML = "";

    const payload = readForm(form);
    const ticketNumber = payload.ticketNumber?.trim();
    if (!ticketNumber) return toast("Missing ticket number", "Enter a ticket number.", "error");

    const newStatus = payload.status;
    const assignedAgent = payload.assignedAgent;
    const resolutionText = payload.resolutionText || "";

    if (newStatus === "RESOLVED" && !resolutionText.trim()) {
      return toast("Resolution required", "Resolution note is required when status is RESOLVED.", "error");
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    setLoading(submitBtn, true);
    try {
      const found = await lookupByTicket(ticketNumber);
      let updated = found;

      const patchRes = await fetch(`${API_BASE}/cases/${encodeURIComponent(found.id)}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: newStatus, assignedAgent })
      });
      const patchData = await patchRes.json();
      if (!patchRes.ok) throw new Error(patchData.message || "Status update failed");
      updated = patchData;

      if (newStatus === "RESOLVED") {
        const resRes = await fetch(`${API_BASE}/cases/${encodeURIComponent(found.id)}/resolution`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ resolutionText })
        });
        const resData = await resRes.json();
        if (!resRes.ok) throw new Error(resData.message || "Resolution update failed");
        updated = resData;
      }

      preview.appendChild(renderCaseCard(updated));
      toast("Update applied", `Ticket ${updated.ticketNumber} updated.`, "success");
      renderTimeline(qs("#timelinePreview"), updated.status);
    } catch (e) {
      toast("Update failed", e.message || String(e), "error");
    } finally {
      setLoading(submitBtn, false);
    }
  });
}

function connectThemeToggle() {
  const btn = qs("#btnTheme");
  const icon = btn.querySelector("i");
  const stored = localStorage.getItem("airline-theme");
  if (stored === "light") document.body.classList.add("light");

  btn.addEventListener("click", () => {
    document.body.classList.toggle("light");
    const isLight = document.body.classList.contains("light");
    localStorage.setItem("airline-theme", isLight ? "light" : "dark");
    icon.className = isLight ? "fa-solid fa-moon" : "fa-solid fa-sun";
  });
}

async function loadKpis() {
  try {
    const sRes = await fetch(`${API_BASE}/analytics/summary`);
    const summary = await sRes.json();
    if (!sRes.ok) throw new Error(summary.message || "KPI load failed");

    qs("#kpiTotal").textContent = summary.totalCases ?? 0;
    qs("#kpiHot").textContent = summary.highPriorityCount ?? 0;
    qs("#kpiResolved").textContent = summary.resolvedCount ?? 0;

    const sc = summary.statusCounts || {};
    const activeStatus =
      (sc.IN_PROGRESS > 0 ? "IN_PROGRESS" :
        sc.TRIAGED > 0 ? "TRIAGED" :
          sc.SUBMITTED > 0 ? "SUBMITTED" :
            "SUBMITTED");
    renderTimeline(qs("#timelinePreview"), activeStatus);
  } catch {
    // ignore
  }
}

function connectResetButtons() {
  const resetFeedback = qs("#resetFeedback");
  const resetComplaint = qs("#resetComplaint");
  if (resetFeedback) {
    resetFeedback.addEventListener("click", () => {
      const form = qs("#feedbackForm");
      if (form) form.reset();
      const p = qs("#feedbackTriagePreview");
      if (p) p.innerHTML = `<div class="meta"><strong>Auto-triage preview</strong>: start typing your message.</div>`;
    });
  }
  if (resetComplaint) {
    resetComplaint.addEventListener("click", () => {
      const form = qs("#complaintForm");
      if (form) form.reset();
      const p = qs("#complaintTriagePreview");
      if (p) p.innerHTML = `<div class="meta"><strong>Auto-triage preview</strong>: start typing your message.</div>`;
    });
  }
}

function debounce(fn, waitMs) {
  let t = null;
  return (...args) => {
    clearTimeout(t);
    t = setTimeout(() => fn(...args), waitMs);
  };
}

function sentimentLabel(score) {
  const s = Number(score || 0);
  if (s >= 2) return "Positive";
  if (s <= -2) return "Negative";
  return "Mixed";
}

function connectTriagePreview(formSelector, previewSelector) {
  const form = qs(formSelector);
  const previewEl = qs(previewSelector);
  if (!form || !previewEl) return;

  const textarea = form.querySelector("textarea[name='message']");
  if (!textarea) return;

  const run = debounce(async () => {
    const msg = textarea.value.trim();
    if (!msg) {
      previewEl.innerHTML = `<div class="meta"><strong>Auto-triage preview</strong>: start typing your message.</div>`;
      return;
    }

    try {
      previewEl.innerHTML = `<div class="meta"><strong>Auto-triage preview</strong>: analyzing...</div>`;
      const res = await fetch(`${API_BASE}/triage/preview`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: msg })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || "Triage preview failed");

      const pr = badgeForPriority(data.priority);
      const sent = sentimentLabel(data.sentimentScore);
      previewEl.innerHTML = `
        <div class="meta" style="margin-bottom: 8px;"><strong>Auto-triage preview</strong></div>
        <div style="display:flex; gap:10px; flex-wrap:wrap;">
          <span class="badge"><span class="dot"></span>${escapeHtml(data.category)}</span>
          <span class="badge ${pr.cls}"><span class="dot"></span>${escapeHtml(sent)} sentiment (${data.sentimentScore})</span>
          <span class="badge ${pr.cls}"><span class="dot"></span>${escapeHtml(pr.label)} priority (${data.priority}/5)</span>
        </div>
        <div class="message" style="margin-top:10px;">
          <div class="m-title"><i class="fa-solid fa-wand-magic-sparkles"></i> Suggested resolution plan</div>
          <div>${escapeHtml(data.triageSuggestion || "")}</div>
        </div>
      `;
    } catch (e) {
      previewEl.innerHTML = `<div class="meta"><strong>Auto-triage preview</strong>: unable to analyze right now.</div>`;
    }
  }, 380);

  textarea.addEventListener("input", run);
}

function connectDefaultDates() {
  // default journey date = today, but allow user to change
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const dd = String(today.getDate()).padStart(2, "0");
  const iso = `${yyyy}-${mm}-${dd}`;

  qsa("input[name='journeyDate']").forEach(el => {
    if (!el.value) el.value = iso;
  });
}

function init() {
  connectTabs();
  connectTrack();
  connectAdmin();
  connectThemeToggle();
  trackToggle();
  connectResetButtons();
  connectDefaultDates();
  connectTriagePreview("#feedbackForm", "#feedbackTriagePreview");
  connectTriagePreview("#complaintForm", "#complaintTriagePreview");

  const refreshBtn = qs("#btnRefreshAnalytics");
  if (refreshBtn) {
    refreshBtn.addEventListener("click", () => loadAnalytics(true));
  }

  // Optional forms (only exist on /customer page).
  const feedbackForm = qs("#feedbackForm");
  if (feedbackForm) {
    feedbackForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      await handleCreateCase(e.currentTarget, "/feedback");
    });
  }
  const complaintForm = qs("#complaintForm");
  if (complaintForm) {
    complaintForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      await handleCreateCase(e.currentTarget, "/complaints");
    });
  }

  renderTimeline(qs("#timelinePreview"), "SUBMITTED");

  // Default active tab on refresh (important after removing feedback/complaint panels).
  try {
    const validTabIds = new Set(qsa(".tab").map(b => b.dataset.tab));
    const storedTab = localStorage.getItem("airline-active-tab");
    const initialTab = (storedTab && validTabIds.has(storedTab)) ? storedTab : "trackTab";
    setTab(initialTab);
    if (initialTab === "analyticsTab") loadAnalytics(true);
  } catch {
    setTab("trackTab");
  }

  loadKpis();

  // Keep dashboard fresh while someone submits from the customer page.
  setInterval(() => {
    // KPI refresh
    loadKpis();

    // Analytics refresh only when visible
    const analyticsTab = qs("#analyticsTab");
    if (analyticsTab && !analyticsTab.classList.contains("hidden")) {
      loadAnalytics(true);
    }
  }, 15000);

  // Delete actions (Analytics only). Uses event delegation so it works after re-render.
  document.addEventListener("click", async (e) => {
    const btn = e.target && e.target.closest ? e.target.closest("[data-delete-id]") : null;
    if (!btn) return;

    const id = btn.dataset.deleteId;
    if (!id) return;

    const ok = confirm("Delete this case? This cannot be undone (demo mode).");
    if (!ok) return;

    try {
      await fetch(`${API_BASE}/cases/${encodeURIComponent(id)}`, { method: "DELETE" });
      toast("Deleted", "Case removed.", "success");
      analyticsLoadedOnce = false;
      loadAnalytics(true);
      loadKpis();
    } catch (err) {
      toast("Delete failed", (err && err.message) ? err.message : String(err), "error");
    }
  });
}

init();

