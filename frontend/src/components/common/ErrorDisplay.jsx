import { useState } from "react";

export default function useError() {
  const [error, setError] = useState("");

  const showError = (message) => {
    setError(message);
    setTimeout(() => setError(""), 3000);
  };

  const ErrorDisplay = () => {
    if (!error) return null;
    return <p style={{ color: "red" }}>{error}</p>;
  };

  return { ErrorDisplay, showError };
}
