import React, { useEffect, useRef, useState } from "react";
import { QRCodeSVG } from "qrcode.react";
import { BrowserQRCodeReader } from "@zxing/browser";
import axiosClient from "../../api/axiosClient";
import { searchReservations } from "../../api/reservationService";
import { usePageHeader } from "../../context/PageHeaderContext";

export default function VerifyQrPage() {
  const { setHeader } = usePageHeader();
  const videoRef = useRef(null);
  const controlsRef = useRef(null);
  const scanRunRef = useRef(0);

  const [criteria, setCriteria] = useState("");
  const [reservations, setReservations] = useState([]);
  const [selectedId, setSelectedId] = useState("");
  const [scannedCode, setScannedCode] = useState("");
  const [verified, setVerified] = useState(null);
  const [cameraOn, setCameraOn] = useState(false);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");

  const selected = reservations.find((r) => r.id === selectedId);

  useEffect(() => {
    setHeader({
      title: "Verify QR & Finalize",
      breadcrumb: "solara-grid / field ops / qr-verify",
    });

    return () => controlsRef.current?.stop();
  }, [setHeader]);

  function stopCamera() {
    scanRunRef.current += 1;
    controlsRef.current?.stop();
    controlsRef.current = null;
    setCameraOn(false);
  }

  async function findReservations(event) {
    event.preventDefault();
    if (!criteria.trim()) return;

    stopCamera();
    setBusy(true);
    setMessage("");
    setVerified(null);
    setScannedCode("");
    setReservations([]);
    setSelectedId("");

    try {
      const response = await axiosClient.get("/reservations/approved-for-qr", {
        params: { criteria: criteria.trim() },
      });
      const results = (response.data ?? []).filter(
        (r) => r.status === "Approved" && r.qrCode,
      );

      setReservations(results);
      setSelectedId(results[0]?.id ?? "");

      if (results.length === 0) {
        setMessage(
          "No approved reservations with QR codes matched this search.",
        );
      }
    } catch (error) {
      setMessage(
        error.response?.data?.message || "Could not load reservations.",
      );
    } finally {
      setBusy(false);
    }
  }

  function chooseReservation(id) {
    stopCamera();
    setSelectedId(id);
    setScannedCode("");
    setVerified(null);
    setMessage("");
  }

  async function startCamera() {
    if (!selected || cameraOn) return;

    const run = ++scanRunRef.current;
    setScannedCode("");
    setVerified(null);
    setMessage("");
    setCameraOn(true);

    try {
      // Let React display the video element before attaching the camera.
      await new Promise((resolve) => requestAnimationFrame(resolve));

      const reader = new BrowserQRCodeReader();
      const controls = await reader.decodeFromVideoDevice(
        undefined,
        videoRef.current,
        (result, _error, activeControls) => {
          if (!result || scanRunRef.current !== run) return;

          scanRunRef.current += 1;
          activeControls.stop();
          controlsRef.current = null;
          setCameraOn(false);
          setScannedCode(result.getText());
          setMessage("QR scanned. Select Verify against server.");
        },
      );

      if (scanRunRef.current !== run) {
        controls.stop();
      } else {
        controlsRef.current = controls;
      }
    } catch {
      if (scanRunRef.current === run) {
        setCameraOn(false);
        setMessage(
          "Could not open the camera. Check browser camera permission.",
        );
      }
    }
  }

  async function verifyQr() {
    if (!selected || !scannedCode) return;

    if (scannedCode !== selected.qrCode) {
      setMessage("The scanned QR does not match the selected reservation.");
      return;
    }

    setBusy(true);
    setMessage("");

    try {
      const response = await axiosClient.post("/qr/verify", {
        qrCode: scannedCode,
      });

      if (
        response.data?.verified !== true ||
        response.data?.reservationId !== selected.id
      ) {
        throw new Error("The server did not verify this reservation.");
      }

      setVerified(response.data);
      setMessage("QR verified against the server.");
    } catch (error) {
      setVerified(null);
      setMessage(
        error.response?.data?.message ||
          error.message ||
          "QR verification failed.",
      );
    } finally {
      setBusy(false);
    }
  }

  async function finalizeTransfer() {
    if (!verified?.reservationId) return;

    setBusy(true);
    setMessage("");

    try {
      const response = await axiosClient.post("/qr/finalize-transfer", {
        reservationId: verified.reservationId,
      });

      if (response.data?.success !== true) {
        throw new Error("The server did not complete the transfer.");
      }

      setMessage(`Reservation ${verified.reservationId} completed.`);
      setReservations((current) =>
        current.filter((r) => r.id !== verified.reservationId),
      );
      setSelectedId("");
      setScannedCode("");
      setVerified(null);
    } catch (error) {
      setMessage(
        error.response?.data?.message ||
          error.message ||
          "Could not finalize the transfer.",
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="mx-auto max-w-7xl">
      <form onSubmit={findReservations} className="mb-6 flex flex-wrap gap-3">
        <input
          value={criteria}
          onChange={(event) => setCriteria(event.target.value)}
          placeholder="Search by prosumer NIC, node, or slot"
          className="min-w-64 flex-1 rounded border border-slate-700 bg-slate-900 px-4 py-2 text-white"
        />
        <button
          type="submit"
          disabled={busy || !criteria.trim()}
          className="rounded bg-amber-500 px-5 py-2 font-semibold text-slate-950 disabled:opacity-50"
        >
          Find approved reservation
        </button>
      </form>

      {reservations.length > 0 && (
        <select
          value={selectedId}
          onChange={(event) => chooseReservation(event.target.value)}
          className="mb-6 w-full rounded border border-slate-700 bg-slate-900 px-4 py-2 text-white"
        >
          {reservations.map((r) => (
            <option key={r.id} value={r.id}>
              {r.prosumerNic} · {r.nodeId} · {r.id}
            </option>
          ))}
        </select>
      )}

      {message && (
        <p
          role="status"
          className="mb-5 rounded border border-slate-700 bg-slate-900 p-4 text-sm text-slate-200"
        >
          {message}
        </p>
      )}

      <div className="grid gap-6 xl:grid-cols-[1.5fr_1fr]">
        <section className="rounded border border-slate-800 bg-slate-900">
          <div className="border-b border-slate-800 px-6 py-5">
            <h2 className="font-semibold text-white">Reservation QR</h2>
            <p className="text-sm text-slate-400">
              Server-issued code for the selected approved reservation
            </p>
          </div>

          <div className="p-6">
            <div className="flex min-h-64 flex-col items-center justify-center rounded border border-dashed border-teal-700 p-6">
              {selected ? (
                <>
                  <div className="rounded bg-white p-3">
                    <QRCodeSVG
                      value={selected.qrCode}
                      size={200}
                      level="M"
                      marginSize={4}
                      title="Reservation QR code"
                    />
                  </div>
                  <p className="mt-4 text-center text-sm text-slate-400">
                    Reservation {selected.id}
                  </p>
                </>
              ) : (
                <p className="text-center text-slate-400">
                  Search for an approved reservation to display its QR code.
                </p>
              )}
            </div>

            <div className="mt-6 border-t border-slate-800 pt-5">
              <h3 className="font-medium text-white">Scan the prosumer’s QR</h3>
              <p className="mt-1 text-sm text-slate-400">
                Use your camera to read the code shown by the prosumer.
              </p>

              <video
                ref={videoRef}
                muted
                playsInline
                className={`mt-4 w-full rounded bg-slate-950 ${
                  cameraOn ? "" : "hidden"
                }`}
              />

              <div className="mt-4 flex flex-wrap gap-3">
                <button
                  type="button"
                  onClick={startCamera}
                  disabled={!selected || cameraOn || busy}
                  className="rounded bg-amber-500 px-4 py-2 font-semibold text-slate-950 disabled:opacity-50"
                >
                  Start camera
                </button>

                {cameraOn && (
                  <button
                    type="button"
                    onClick={stopCamera}
                    className="rounded border border-slate-600 px-4 py-2 text-white"
                  >
                    Stop camera
                  </button>
                )}

                <button
                  type="button"
                  onClick={verifyQr}
                  disabled={!scannedCode || !selected || busy || !!verified}
                  className="rounded border border-teal-600 px-4 py-2 text-teal-300 disabled:opacity-50"
                >
                  Verify against server
                </button>
              </div>
            </div>
          </div>
        </section>

        <section className="self-start rounded border border-slate-800 bg-slate-900">
          <div className="border-b border-slate-800 px-6 py-5">
            <h2 className="font-semibold text-white">Verified reservation</h2>
            <p className="text-sm text-slate-400">
              Details appear after successful server verification
            </p>
          </div>

          <div className="space-y-4 p-6 text-sm">
            {verified && selected ? (
              <>
                <p className="font-mono text-teal-400">
                  Verified · {verified.reservationId}
                </p>
                <p>
                  Prosumer NIC: <strong>{selected.prosumerNic}</strong>
                </p>
                <p>
                  Node: <strong>{selected.nodeId}</strong>
                </p>
                <p>
                  Battery slot: <strong>{selected.batterySlotId}</strong>
                </p>
                <p>
                  Scheduled:{" "}
                  <strong>
                    {new Date(selected.scheduledDateTime).toLocaleString()}
                  </strong>
                </p>
              </>
            ) : (
              <p className="text-slate-400">
                Scan and verify a reservation before finalizing its transfer.
              </p>
            )}

            <button
              type="button"
              onClick={finalizeTransfer}
              disabled={!verified || busy}
              className="w-full rounded bg-amber-500 px-4 py-3 font-semibold text-slate-950 disabled:opacity-50"
            >
              {busy ? "Please wait…" : "Finalize energy transfer"}
            </button>
          </div>
        </section>
      </div>
    </div>
  );
}
