/* eslint-disable quotes */
export default async function makeQuerry(address, body) {
  const response = await fetch(`http://localhost:3000/${address}`, {
    headers: {
      "Content-Type": "application/json",
    },
    credentials: 'include',
    method: 'POST',
    body,
  });
  return { status: response.status };
}
