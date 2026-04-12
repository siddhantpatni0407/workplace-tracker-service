# Support API: Decrypt Password

Purpose
-------
This admin-only API allows authorized administrators to decrypt AES-encrypted passwords stored in the system. Use this
endpoint only for support/debug purposes. Returning plaintext passwords is a significant security risk; access to this
endpoint should be restricted and audited.

Endpoint
--------
POST /api/v1/workplace-tracker-service/support/decrypt-password

Request
-------
Content-Type: application/json
Authorization: Bearer <admin-jwt>

Body:
{
"encryptedPassword": "<base64-encrypted-string>",
"keyVersion": 1
}

Response
--------
200 OK
{
"status": "SUCCESS",
"message": "Decryption successful",
"data": {
"encryptedPassword": "u2PR46m2_975nBKm8kobdQ==",
"decryptedPassword": "12345",
"keyVersion": 1
}
}

Errors
------

- 400 Bad Request — missing fields or invalid input
- 403 Forbidden — caller does not have ADMIN role
- 500 Internal Server Error — decryption failure (invalid key/corrupted data)

Security
--------

- This endpoint MUST only be accessible to ADMIN users. The controller enforces this via role-based annotation.
- All calls should be logged and audited. Avoid returning or storing decrypted passwords beyond the immediate response.

Implementation notes
--------------------

- Controller: `com.sid.app.controller.SupportController`
- Service: `com.sid.app.service.SupportService`
- DTO: `com.sid.app.model.DecryptPasswordRequest`
- Uses `com.sid.app.utils.AESUtils.decrypt(encrypted, keyVersion)`

