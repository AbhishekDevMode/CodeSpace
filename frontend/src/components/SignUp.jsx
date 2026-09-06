import React, { useState } from "react";

const SignUp = () => {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");

    try {
      const response = await fetch("http://localhost:8080/api/auth/signup", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name, email, password }),
      });

      if (!response.ok) {
        throw new Error("Failed to sign up");
      }

      const data = await response.json();
      console.log("Sign up successful:", data);
    } catch (err) {
      setErrorMsg(err.message);
    }
  };

  return (
    <div className="flex flex-col justify-center">
      <form
        onSubmit={handleSubmit}
        className="w-full h-full border-2 border-green-200 flex flex-col justify-center items-center gap-2 p-4"
      >
        {errorMsg && <p className="text-red-500">{errorMsg}</p>}

        <label htmlFor="name">Name</label>
        <input
          id="name"
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Enter your name"
          className="border-2 border-blue-100 p-1"
          required
        />

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
          Sign Up
        </button>
      </form>
    </div>
  );
};

export default SignUp;