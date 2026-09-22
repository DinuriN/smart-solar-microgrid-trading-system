import axiosClient from "./axiosClient";

const BASE = "/reservations";

// Shared search — backend scopes results by the caller's role automatically
export const searchReservations = (criteria) =>
  axiosClient.get(`${BASE}/search`, { params: { criteria } });

// BackOffice — read-only queue, optional status filter
export const getBackOfficeQueue = (status) =>
  axiosClient.get(`${BASE}/queue`, { params: status ? { status } : {} });

// BackOffice — approve / block (owned by Member 4, called from this UI)
export const approveReservation = (id) =>
  axiosClient.patch(`${BASE}/${id}/approve`);

export const blockReservation = (id, reason) =>
  axiosClient.patch(`${BASE}/${id}/block`, { reason });

// Grid Operator — reservations on assigned nodes
export const getOperatorReservations = (operatorId, status) =>
  axiosClient.get(`${BASE}/operator/${operatorId}`, { params: status ? { status } : {} });

export const getNextForOperator = (operatorId) =>
  axiosClient.get(`${BASE}/operator/${operatorId}/next`);

// Shared create / modify / cancel — Prosumer + Grid Operator
export const createReservation = (payload) =>
  axiosClient.post(BASE, payload);

export const updateReservation = (id, payload) =>
  axiosClient.put(`${BASE}/${id}`, payload);

export const cancelReservation = (id) =>
  axiosClient.delete(`${BASE}/${id}`);