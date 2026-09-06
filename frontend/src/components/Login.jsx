import React, { useState } from "react";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorMsg("");

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error("Failed to log in");
      }

      const data = await response.json();
      
      if (data.token) {
        localStorage.setItem("token", data.token);
      }

      console.log("Login successful:", data);
    } catch (err) {
      setErrorMsg(err.message);
    }
  };

  return (
    <div className="flex flex-col justify-center">
      <form
        onSubmit={handleLogin}
        className="w-full h-full border-2 border-green-200 flex flex-col justify-center items-center gap-2 p-4"
      >
        {errorMsg && <p className="text-red-500">{errorMsg}</p>}

        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="Enter your email"
          className="border-2 border-blue-100 p-1"
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Enter your password"
          className="border-2 border-blue-100 p-1"
          required
        />

        <button
          type="submit"
          className="mt-2 bg-blue-500 text-white px-4 py-1 rounded"
        >
          Login
        </button>
      </form>
    </div>
  );
};

export default Login;