const API_BASE = "/api";
const CUSTOMER_TAB_KEY = "airline-customer-active-tab";

function qs(sel) {
  return document.querySelector(sel);
}
function qsa(sel) {
  return Array.from(document.querySelectorAll(sel));
}

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
    if (icon.className.includes("fa-spin"))
      icon.className = "fa-solid fa-paper-plane";
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

function badgeForPriority(priority) {
  const p = Number(priority || 1);
  if (p >= 5) return { cls: "bad", label: "Urgent" };
  if (p >= 4) return { cls: "bad", label: "High" };
  if (p >= 3) return { cls: "warn", label: "Medium" };
  return { cls: "good", label: "Low" };
}

function setTab(tabId) {
  qsa(".tab").forEach((btn) => {
    const is = btn.dataset.tab === tabId;
    btn.classList.toggle("active", is);
    btn.setAttribute("aria-selected", is ? "true" : "false");
  });

  qsa(".tab-panel").forEach((panel) => {
    panel.classList.add("hidden");
    if (panel.id === tabId) panel.classList.remove("hidden");
  });

  try {
    localStorage.setItem(CUSTOMER_TAB_KEY, tabId);
    history.replaceState(null, "", `#${tabId}`);
  } catch {
    // ignore
  }
}

function connectTabs() {
  qsa(".tab").forEach((btn) => {
    btn.addEventListener("click", () => setTab(btn.dataset.tab));
  });
}

function statusOrder() {
  return ["SUBMITTED", "TRIAGED", "IN_PROGRESS", "RESOLVED"];
}

function renderTimeline(container, status) {
  if (!container) return;
  const order = statusOrder();
  const activeIdx = Math.max(0, order.indexOf(status));
  container.innerHTML = "";

  const steps = [
    { key: "SUBMITTED", name: "Submitted", sub: "Case created + auto triage" },
    { key: "TRIAGED", name: "Triaged", sub: "Category + priority set" },
    { key: "IN_PROGRESS", name: "In Progress", sub: "Agent working on it" },
    { key: "RESOLVED", name: "Resolved", sub: "Resolution shared" },
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
  const statusBadge = `
    <span class="badge">
      <span class="dot"></span>${escapeHtml(c.status || "")}
    </span>
  `;

  const resolved = c.status === "RESOLVED";
  const body = resolved
    ? `<div class="resolution">
         <div class="m-title"><i class="fa-solid fa-circle-check"></i> Resolution</div>
         <div class="res-body">${escapeHtml(c.resolutionText || "Resolution provided.")}</div>
       </div>`
    : `<div class="resolution" style="border-color: rgba(96,165,250,.22); background: rgba(96,165,250,.07);">
         <div class="m-title" style="color: rgba(96,165,250,.95);"><i class="fa-solid fa-hourglass-half"></i> Next Update</div>
         <div class="res-body">${escapeHtml(c.triageSuggestion || "We’re working on this case.")}</div>
       </div>`;

  const ageH = c.createdAt ? hoursBetween(c.createdAt) : 0;
  const slaH = slaDeadlineHours(c.type);
  const progress = resolved
    ? 100
    : Math.round(Math.min(100, (ageH / slaH) * 100));
  const overdue = !resolved && ageH > slaH;

  const slaHtml = `
    <div class="meta" style="display:flex; gap:12px; align-items:center; margin-top: 10px;">
      <span><strong>SLA</strong>: ${c.type === "COMPLAINT" ? "48h" : "24h"}</span>
      <span><strong>Age</strong>: ${ageH.toFixed(1)}h</span>
      <span style="margin-left:auto; color:${overdue ? "rgba(251,113,133,.98)" : "var(--muted)"};">
        ${overdue ? "<strong>Overdue</strong>" : `Progress: ${progress}%`}
      </span>
    </div>
  `;

  const node = document.createElement("div");
  node.className = "case-card";
  node.innerHTML = `
    <div class="top">
      <div>
        <div class="ticket">${escapeHtml(c.ticketNumber || "")}</div>
        <div class="meta">
          <strong>${escapeHtml(c.type === "COMPLAINT" ? "Complaint" : "Feedback")}</strong>
          • ${escapeHtml(c.category || "")}
        </div>
        ${slaHtml}
      </div>
      <div style="display:flex; flex-direction:column; gap:10px; align-items:flex-end;">
        ${statusBadge}
        <span class="badge ${badge.cls}">
          <span class="dot"></span>${escapeHtml(badge.label)} Priority (${c.priority}/5)
        </span>
      </div>
    </div>

    <div class="case-grid">
      <div>
        <div class="meta"><strong>Customer</strong>: ${escapeHtml(c.customerEmail || "")}</div>
        <div class="meta" style="margin-top:8px;"><strong>Flight</strong>: ${escapeHtml(c.flightNumber || "-")} • ${escapeHtml(c.journeyDate || "-")}</div>
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
    ${body}
  `;

  setTimeout(
    () => renderTimeline(node.querySelector(`#timeline-${c.id}`), c.status),
    0,
  );
  return node;
}

async function lookupByTicket(ticketNumber) {
  const res = await fetch(
    `${API_BASE}/cases/lookup?ticketNumber=${encodeURIComponent(ticketNumber)}`,
  );
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || "Lookup failed");
  return data;
}

async function lookupByEmail(email, limit) {
  const res = await fetch(
    `${API_BASE}/cases/my?email=${encodeURIComponent(email)}&limit=${encodeURIComponent(limit)}`,
  );
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || "Lookup failed");
  return data;
}

function connectTrack() {
  const ticketArea = qs("#trackTicketArea");
  const emailArea = qs("#trackEmailArea");

  qsa("input[name='trackMode']").forEach((r) => {
    r.addEventListener("change", () => {
      const mode = qsa("input[name='trackMode']").find((x) => x.checked).value;
      ticketArea && ticketArea.classList.toggle("hidden", mode !== "ticket");
      emailArea && emailArea.classList.toggle("hidden", mode !== "email");
    });
  });

  qs("#btnLookup")?.addEventListener("click", async () => {
    const ticketNumber = qs("#trackTicketNumber").value.trim();
    if (!ticketNumber)
      return toast("Missing ticket", "Enter a ticket number.", "error");
    try {
      toast("Loading case...", ticketNumber);
      const c = await lookupByTicket(ticketNumber);
      renderCaseResults([c]);
    } catch (e) {
      toast("Lookup failed", e.message || String(e), "error");
    }
  });

  qs("#btnLookupEmail")?.addEventListener("click", async () => {
    const email = qs("#trackEmail").value.trim();
    const limit = Number(qs("#trackLimit").value || 10);
    if (!email) return toast("Missing email", "Enter an email.", "error");
    try {
      const cases = await lookupByEmail(email, limit);
      renderCaseResults(cases || []);
    } catch (e) {
      toast("Lookup failed", e.message || String(e), "error");
    }
  });
}

function renderCaseResults(cases) {
  const out = qs("#caseResults");
  if (!out) return;
  out.innerHTML = "";
  if (!cases || cases.length === 0) {
    out.innerHTML = `<div class="case-card"><div class="meta"><strong>No cases found.</strong></div></div>`;
    return;
  }
  for (const c of cases) out.appendChild(renderCaseCard(c));
}

function connectResetButtons() {
  const resetFeedback = qs("#resetFeedback");
  const resetComplaint = qs("#resetComplaint");

  if (resetFeedback) {
    resetFeedback.addEventListener("click", () => {
      qs("#feedbackForm").reset();
    });
  }

  if (resetComplaint) {
    resetComplaint.addEventListener("click", () => {
      qs("#complaintForm").reset();
    });
  }
}

function connectDefaultDates() {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const dd = String(today.getDate()).padStart(2, "0");
  const iso = `${yyyy}-${mm}-${dd}`;

  qsa("input[name='journeyDate']").forEach((el) => {
    if (!el.value) el.value = iso;
  });
}

function connectThemeToggle() {
  const btn = qs("#btnTheme");
  if (!btn) return;

  const icon = btn.querySelector("i");
  const stored = localStorage.getItem("airline-theme");
  const mode = stored === "light" ? "light" : "dark";
  document.documentElement.setAttribute("data-theme", mode);
  document.body.classList.toggle("light", mode === "light");
  if (icon) {
    icon.className = mode === "light" ? "fa-solid fa-sun" : "fa-solid fa-moon";
  }

  btn.addEventListener("click", () => {
    const current =
      document.documentElement.getAttribute("data-theme") || "dark";
    const next = current === "light" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", next);
    document.body.classList.toggle("light", next === "light");
    localStorage.setItem("airline-theme", next);
    if (icon) {
      icon.className =
        next === "light" ? "fa-solid fa-sun" : "fa-solid fa-moon";
    }
  });
}

async function ensureCustomerSession() {
  try {
    const res = await fetch(`${API_BASE}/auth/me`);
    if (!res.ok) {
      location.href = "/customer/login.html";
      return false;
    }

    const me = await res.json();
    if (me.role !== "CUSTOMER") {
      location.href = me.role === "ADMIN" ? "/" : "/customer/login.html";
      return false;
    }

    const userTag = qs("#authUser");
    if (userTag) {
      userTag.innerHTML = `<i class="fa-solid fa-user"></i> ${escapeHtml(me.username || "Customer")}`;
    }
    return true;
  } catch {
    location.href = "/customer/login.html";
    return false;
  }
}

function connectLogout(redirectPath) {
  const btn = qs("#btnLogout");
  if (!btn) return;
  btn.addEventListener("click", async () => {
    try {
      await fetch(`${API_BASE}/auth/logout`, { method: "POST" });
    } finally {
      location.href = redirectPath;
    }
  });
}

async function handleCreateCase(form, endpoint, kindLabel) {
  const btn = form.querySelector('button[type="submit"]');
  setLoading(btn, true);
  try {
    const payload = {};
    for (const input of form.querySelectorAll("[name]")) {
      if (input.type === "checkbox") continue;
      payload[input.name] = input.value;
    }

    const res = await fetch(API_BASE + endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const data = await res.json();
    if (!res.ok)
      throw new Error(data.message || `${kindLabel} submission failed`);

    toast(
      "Submitted",
      `${kindLabel} created. Ticket: ${data.ticketNumber}`,
      "success",
    );
  } catch (e) {
    toast("Submission failed", e.message || String(e), "error");
  } finally {
    setLoading(btn, false);
  }
}

async function init() {
  const allowed = await ensureCustomerSession();
  if (!allowed) return;

  connectTabs();
  connectThemeToggle();
  connectLogout("/customer/login.html");
  connectResetButtons();
  connectDefaultDates();
  connectTrack();

  const feedbackForm = qs("#feedbackForm");
  const complaintForm = qs("#complaintForm");

  if (feedbackForm) {
    feedbackForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      await handleCreateCase(feedbackForm, "/feedback", "Feedback");
    });
  }

  if (complaintForm) {
    complaintForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      await handleCreateCase(complaintForm, "/complaints", "Complaint");
    });
  }

  try {
    const validTabIds = new Set(qsa(".tab").map((b) => b.dataset.tab));
    const hashTab = (window.location.hash || "").replace("#", "").trim();
    const storedTab = localStorage.getItem(CUSTOMER_TAB_KEY);
    const initialTab =
      hashTab && validTabIds.has(hashTab)
        ? hashTab
        : storedTab && validTabIds.has(storedTab)
          ? storedTab
          : "feedbackTab";
    setTab(initialTab);
  } catch {
    setTab("feedbackTab");
  }
}

init();
