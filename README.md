# JSON Time-Stamp Authority service

Time stamping is an increasingly valuable complement to digital signing practices, 
enabling organizations to record when a digital item—such as a message, document, 
transaction or piece of software—was signed. For some applications, the timing of 
a digital signature is critical, as in the case of stock trades, lottery ticket 
issuance and some legal proceedings. Even when time is not intrinsic to the 
application, time stamping is helpful for record keeping and audit processes, 
because it provides a mechanism to prove whether the digital certificate was valid 
at the time it was used.

### A modern take

While [Roughtime](https://tools.ietf.org/id/draft-roughtime-aanchal-00.html) is a new draft, 
it is still based on a binary protocol which can be a hindrance against adoption. The protocol
specifies a request which includes a nonce which is included in the signed response.

This project provides the same data points while utilising modern technologies to achieve
the same goals. Using the proposed standard [RFC 7515](https://tools.ietf.org/html/rfc7515)
to encode a signed time stamp from the Time-Stamp Authority.

## Requirements

The TSA is REQUIRED:

   1.    to use a trustworthy source of time.
   2.    to include a trustworthy time value for each time-stamp token.
   3.    to produce a time-stamp token upon receiving a valid request
         from the requester, when it is possible.
   4.    not to examine the message being time-stamped in any way.
   5.    not to include any identification of the requesting entity in
         the time-stamp tokens.
   6.    to sign each time-stamp token using a key generated exclusively
         for this purpose and have this property of the key indicated on
         the corresponding certificate.

These requirements are an abbreviated form of [RFC 3161](https://www.ietf.org/rfc/rfc3161.txt).

## Time Stamp Request and Response Formats

The service provides a `/sign` endpoint for time stamping requests, and a `/cert` 
endpoint for getting an out of band certificate which can be used to verify signed 
responses.

### Request Format

A time-stamping request a HTTP POST request which has the following body:

TimeStampRequest:

 - version: 2 at the time of this writing
 - message: The message to be time-stamped, which can be any json value.
 - nonce: The nonce is a large random number with a high probability that the client 
   generates it only once (e.g., a 128 bit integer).

Example:

```json
{
  "version": 2,
  "message": {
    "some": "value",
    "other": "value"
  },
  "nonce": "3455345232345454"
}
```

### Response Format

The response is in the format of a JSON Web Signature, as described in 
proposed standard [RFC 7515](https://tools.ietf.org/html/rfc7515). The 
response is comprised of three parts, all base 64 encoded and separated by a dot (.)

#### Header

The header is a JSON Unprotected header.

The following fields in the header are currently supported. The
fields are set as follows:

 - typ: application/timestamped-json
 - alg: The signing algorithm, e.g. RS256
 - x5u: a URL that refers to a resource for the X.509 public key certificate
   or certificate chain [RFC5280] corresponding to the key used to digitally sign the
   JWS.
 - jku: a URL that refers to the key used to digitally sign the JWS.  

#### Payload

The payload of the time-stamping response is as follows:

TimeStampResponse:

 - status: StatusInfo
 - timeStampToken: TimeStampToken, optional

StatusInfo:

 - status: One of
   - 0: granted
   - 1: rejection
 - statusInfo: One of
   - badAlg: unrecognized or unsupported Algorithm Identifier
   - badRequest: transaction not permitted or supported
   - badDataFormat: the data submitted has the wrong format
   - timeNotAvailable: the TSA's time source is not available
   - systemFailure: the request cannot be handled due to system failure

TimeStampToken:

 - version: 2 at the time of this writing
 - message: MUST have the same value as the message field in TimeStampRequest
 - genTime: Generalised time as an ISO 3306 string
 - radius: Integer used to indicate the server’s certainty about the reported time.
 - nonce: MUST have the same value as the similar field in TimeStampRequest

Radius:

Radius (in microseconds) is used to indicate the server’s certainty 
about the reported time. For example, a radius of 1,000,000μs means the 
server is reasonably sure that the true time is within one second of the 
reported time

#### Signature

The signature is a cryptographic signature generated as defined in the 
proposed standard [RFC 7515](https://tools.ietf.org/html/rfc7515).

### Example

```
eyJhbGciOiJSUzI1NiIsIng1dSI6Imh0dHBzOi8vdHNhLnN0YXRlZC5hdC9jZXJ0IiwidHlwIjoiYXBwbGljYXRpb24vdGltZXN0YW1wLXJlcGx5K2p3cyIsImprdSI6Imh0dHBzOi8vdHNhLnN0YXRlZC5hdC9qd2sifQ.eyJzdGF0dXMiOnsic3RhdHVzIjowfSwidGltZVN0YW1wVG9rZW4iOnsiY29udGVudFR5cGUiOiJpZC1zaWduZWREYXRhIiwiY29udGVudCI6eyJ2ZXJzaW9uIjoxLCJjb250ZXh0Ijoic29tZSBjb250ZXh0IiwibWVzc2FnZUltcHJpbnQiOiI0MGYxZjMxMGNiYWUwMTE2NDJkMzNlZGJhZDc0MmVhM2M1ODE4NjRkIiwic2VyaWFsTnVtYmVyIjoyNCwiZ2VuVGltZSI6IjIwMTktMTItMTdUMTM6NTI6MjIrMDAwMCIsImFjY3VyYWN5Ijp7InNlY29uZHMiOjAsIm1pbGxpcyI6MCwibWljcm9zIjowfSwib3JkZXJpbmciOmZhbHNlLCJub25jZSI6MzQ1NTM0NTIzMjM0NTQ1NH19fQ.JxWouL-3BRBMCtDHbTk_Rs1X2qT_4n7h0jXQCPzVb-G9nwUpnGm5G2oCy-kCXuFnYjptETFwB0iEilDatJ_dclWO7ob1jPTktnyZIFp496sd1LgDl-Y0g3dUIIvPZMevKg4jt01A2XsCKGGm-WqY_YryZMZ_kj26Y0TyEGdyneN4IVQ4cMz4Y34CzHVl-0iqYzlNR-gvy_1B671LG82qRoA4PE2L3m2YemEAaqhYHr5VPV5LDmuqPoiTjpWwHtXlNU-2jHIpVce6zDAUANFvFMzOC68hxkLCAQQMT8g9LAQAF_P8UjTOXot-MwnQVHh8RSwgOC1L1xE-6FZRqkJnC6rJRwOpzLMDim1cG_e6-mYQR0L-oH_euIebvpIFnwiAQI4fkYCWiRt-ppI89a15kMVyQRCUAASQ8T1T0ooaMv9PuOuvgoI-89v9P5z9RfhYOC6Ps72ZOOaT_nlyQmkHRb6jYEQvii8G1rIKEfc4qRrcH6-TeBQvpA5EahKB7UMJfKhButYMi74ScrO6zMtdJkLxNcaAY91EtVi9FDytoBMGt2pu6PIIEYddLN4xq2GQwHpixYmGKOShnOGK1M56aLIjzjq7Rvxzt6UBgCZnO4UOuHp9YUFg6erAf4UBAu8Bl2_2j5eWVOMfQoTw6mfiU3pRdeD7ADdM45ulDmLcL2o
```

The components can be found by splitting on "." and base 64 decoding them into:

Header:

```json
{
  "alg": "RS256",
  "x5u": "https://tsa.stated.at/cert",
  "typ": "application/timestamped-json",
  "jku": "https://tsa.stated.at/jwk"
}
```

Payload:

```json
{
  "status": {
    "status": 0
  },
  "timeStampToken": {
    "version": 2,
    "message": {
      "some": "value",
      "other": "value"
    },
    "genTime": "2019-12-17T13:52:22+0000",
    "radius": 34588844,
    "nonce": "3455345232345454"
  }
}
```

The signature is binary data which can be used to verify the payload using the certificate
which can be obtained from the corresponding endpoint.

## Certificate Request and Response Formats

The service provides a `/cert` endpoint which provides the certificate containing 
the public key corresponding to the private key used for signing the time stamp
requests.

A GET request to this endpoint will receive a response with the PEM encoded certificate.

Example:

```
-----BEGIN CERTIFICATE-----
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
/aMUJHf5H5quEg5yFLxLfNcK
-----END CERTIFICATE-----
```

## Client Example

The following is an example of time stamping some data in javascript. It is assumed that 
the data exists elsewhere and has been hashed using a sha1 hashing algorithm.

```javascript
const request = require('request-promise');
const jose = require('jose');

(async () => {
  const response = await request({
    method: 'POST',
    uri: 'https://tsa.stated.at/sign',
    body: JSON.stringify({
      version: 2,
      message: {
        some: 'value',
        other: 'value'
      },
      nonce: '234534534534'
    })
  });
  const token = jose.JWT.decode(response, { complete: true });
  const keyset = await request(token.header.jku, { json: true });
  // Get the first key from the set of public keys, for the purposes of this example
  const key = jose.JWK.asKey(keyset.keys[0]);
  const valid = jose.JWS.verify(response, key);
  if (!valid) return;
  const success = token.payload.status.status == 0;
  if (!success) return;
  const timestamp = Date.parse(token.payload.timeStampToken.genTime);
  console.log('signed time stamp: ' + timestamp);
})();
```
