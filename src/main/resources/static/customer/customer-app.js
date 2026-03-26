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
    if (icon.className.includes("fa-spin")) icon.className = "fa-solid fa-paper-plane";
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
}

function connectTabs() {
  qsa(".tab").forEach(btn => {
    btn.addEventListener("click", () => setTab(btn.dataset.tab));
  });
}

function connectResetButtons() {
  const resetFeedback = qs("#resetFeedback");
  const resetComplaint = qs("#resetComplaint");

  if (resetFeedback) {
    resetFeedback.addEventListener("click", () => {
      qs("#feedbackForm").reset();
      const p = qs("#feedbackTriagePreview");
      if (p) p.innerHTML = `<div class="meta"><strong>Auto-triage preview</strong>: start typing your message.</div>`;
    });
  }

  if (resetComplaint) {
    resetComplaint.addEventListener("click", () => {
      qs("#complaintForm").reset();
      const p = qs("#complaintTriagePreview");
      if (p) p.innerHTML = `<div class="meta"><strong>Auto-triage preview</strong>: start typing your message.</div>`;
    });
  }
}

function connectDefaultDates() {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const dd = String(today.getDate()).padStart(2, "0");
  const iso = `${yyyy}-${mm}-${dd}`;

  qsa("input[name='journeyDate']").forEach(el => {
    if (!el.value) el.value = iso;
  });
}

function connectThemeToggle() {
  const btn = qs("#btnTheme");
  if (!btn) return;

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
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || `${kindLabel} submission failed`);

    toast("Submitted", `${kindLabel} created. Ticket: ${data.ticketNumber}`, "success");
  } catch (e) {
    toast("Submission failed", e.message || String(e), "error");
  } finally {
    setLoading(btn, false);
  }
}

function init() {
  connectTabs();
  connectThemeToggle();
  connectResetButtons();
  connectDefaultDates();
  connectTriagePreview("#feedbackForm", "#feedbackTriagePreview");
  connectTriagePreview("#complaintForm", "#complaintTriagePreview");

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
}

init();

