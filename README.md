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

The [RFC 3161](https://www.ietf.org/rfc/rfc3161.txt) specifies a binary protocol for
requesting and providing secure time stamps. The protocol specifies a request which 
includes a hash of the data to be timestamped and a policy under which to time stamp it.

This project provides the same data points while utilising modern technologies to achieve
the same goals. Using the proposed standard [RFC 7515](https://tools.ietf.org/html/rfc7515)
to encode a signed time stamp from the Time-Stamp Authority.

## Requirements

The TSA is REQUIRED:

   1.    to use a trustworthy source of time.
   2.    to include a trustworthy time value for each time-stamp token.
   3.    to include a unique integer for each newly generated time-stamp
         token.
   4.    to produce a time-stamp token upon receiving a valid request
         from the requester, when it is possible.
   5.    to include within each time-stamp token an identifier to
         uniquely indicate the security policy under which the token was
         created.
   6.    to only time-stamp a hash representation of the datum, i.e., a
         data imprint associated with a one-way collision resistant
         hash-function uniquely identified by an OID.
   7.    to examine the OID of the one-way collision resistant hash-
         function and to verify that the hash value length is consistent
         with the hash algorithm.
   8.    not to examine the imprint being time-stamped in any way (other
         than to check its length, as specified in the previous bullet).
   9.    not to include any identification of the requesting entity in
         the time-stamp tokens.
   10.   to sign each time-stamp token using a key generated exclusively
         for this purpose and have this property of the key indicated on
         the corresponding certificate.

These requirements are reused from [RFC 3161](https://www.ietf.org/rfc/rfc3161.txt)
with the notable exclusion of requirement 11 which is a requierment of extensibility.
Requirement 11 is not included at the time of this writing, but the inherent 
extensibility of json should make it relatively easy to add this in a future version.

## Time Stamp Request and Response Formats

The service provides a `/sign` endpoint for time stamping requests.

### Request Format

A time-stamping request a HTTP POST request which has the following body:

TimeStampRequest:

 - version: 1 at the time of this writing
 - messageImprint: The hashing algorithm and the hash of the data to be time-stamped.
 - context: The context in which the data is to be time-stamped. This field
   is optional and will be carried as-is to the response.
 - nonce: The nonce is a large random number with a high probability that the client 
   generates it only once (e.g., a 128 bit integer).

Example:

```json
{
  "version": 1,
  "messageImprint": {
    "hashAlgorithm": "sha1",
    "hashedMessage": "40f1f310cbae011642d33edbad742ea3c581864d"
  },
  "context": "some context",
  "nonce": "3455345232345454"
}
```

### Response Format

The response is in the format of a JSON Web Signature, as described in 
proposed standard [RFC 7515](https://tools.ietf.org/html/rfc7515). The 
response is comprised of three parts, all base 64 encoded and separated by a dot (.)

#### Header

The header is a JSON Unprotected header.

Only the "alg" and "typ" fields in the header are currently supported. The
fields are set as follows:

 - typ: Time-Stamp
 - alg: The signing algorithm, e.g. SHA256withRSA
 - x5u: a URL that refers to a resource for the X.509 public key certificate
   or certificate chain [RFC5280] corresponding to the key used to digitally sign the
   JWS.

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

 - contentType: "id-signedData" at the time of this writing
 - content: TimeStampContent

TimeStampContent:

 - version: 1 at the time of this writing
 - context: MUST have the same value as the similar field in TimeStampRequest
 - messageImprint: MUST have the same value as the similar field in TimeStampRequest
 - serialNumber: Time-Stamping users MUST be ready to accommodate integers up to at least 160 bits
 - genTime: Generalised time in the format YYYYMMDDhhmmss[.s...]Z
 - accuracy: TimeStampAccuracy
 - ordering: If set to true, every time-stamp
   token from the same TSA can always be ordered based on the genTime
   field, regardless of the genTime accuracy.
 - nonce: MUST have the same value as the similar field in TimeStampRequest

TimeStampAccuracy:

 - seconds: Integer
 - millis: Integer
 - micros: Integer

If either seconds, millis or micros is missing, then a value of zero
MUST be taken for the missing field.

By adding the accuracy value to the GeneralizedTime, an upper limit
of the time at which the time-stamp token has been created by the TSA
can be obtained.  In the same way, by subtracting the accuracy to the
GeneralizedTime, a lower limit of the time at which the time-stamp
token has been created by the TSA can be obtained.

Accuracy can be decomposed in seconds, milliseconds (between 1-999)
and microseconds (1-999), all expressed as integer.

#### Signature

The signature is a cryptographic signature generated as defined in the 
proposed standard [RFC 7515](https://tools.ietf.org/html/rfc7515).

### Example

```
eyJ0eXAiOiAiVGltZS1TdGFtcCIsImFsZyI6ICJTSEEyNTZ3aXRoUlNBIiwieDV1IjogImh0dHBzOi8vdHNhLnN0YXRlZC5hdC9jZXJ0In0=.eyJzdGF0dXMiOnsic3RhdHVzIjowfSwidGltZVN0YW1wVG9rZW4iOnsiY29udGVudFR5cGUiOiJpZC1zaWduZWREYXRhIiwiY29udGVudCI6eyJ2ZXJzaW9uIjoxLCJwb2xpY3kiOiJwb2xpY3kxIiwibWVzc2FnZUltcHJpbnQiOiI0MGYxZjMxMGNiYWUwMTE2NDJkMzNlZGJhZDc0MmVhM2M1ODE4NjRkIiwic2VyaWFsTnVtYmVyIjoxLCJnZW5UaW1lIjoiMjAxOTEyMTExNDA2MzJaIiwiYWNjdXJhY3kiOnsic2Vjb25kcyI6MCwibWlsbGlzIjowLCJtaWNyb3MiOjB9LCJvcmRlcmluZyI6ZmFsc2UsIm5vbmNlIjozNDU1MzQ1MjMyMzQ1NDU0fX19.aEyjY1SvA7jDzVmm0kLQ3eL4mgK+WdItzHRpG9wGDPwz2gn8OZU5lM4bbdRSB3kt1GSz89/c4+svOnI0haZJNjSLkZppTOnpUoeAFrqQcDisDAOtdCIkzwMRT9k/Nz5wpHPSbEzPss1VV1r9ozR0tmuTBivX1qIB91Pq/21to1B7PywUbZbBVarDym3vIneomPKXHsdMyBY1p0ZwOWxFr34Ku8OT7XKRyQhDEzRai7pIE6BYhshdexcnHIyL3sGCUtBmvP2r1D9WKJyZt/qrDjQriLk2hEugPuVpuIkkdRauwB6HKbo4QMi6f3UwsoDEpOFCtRhu7TZmYgtRj/JRPinS0MSylqKG99V8dCLO93N1kL/GmQrahcqkPfbgGrfdmUQYpKKjkv99uS5guJYK9b3Hc4sbMuQAPaVNXn9FCJR21lbCMEEGPuw+x+tj42oZAGRMih95jVMOOTcGxovMnMa8yQDlb2v4Y0BMxGO/BD9S6d+aqVYk8loN0w2IAKQ6NnTX6ggg8vNAgfIFsgOGd4KwxjVbQ08KKgWpkelGCluAI0XypTMdun73lwSaOv2XoxssFEJ5zI2RKUw3sgfIStctD6QBX3ubFiK9PUBGACYrxj6NHPmnweVXoEIz9yDAuWDsbWtzfVo3YAH1p+ueGXKjuZgDKE1MBTrLCTSECw4=
```

The components can be found by splitting on "." and base 64 decoding them into:

Header:

```json
{
  "typ": "Time-Stamp",
  "alg": "SHA256withRSA",
  "x5u": "https://tsa.stated.at/cert"
}
```

Payload:

```json
{
  "status": {
    "status": 0
  },
  "timeStampToken": {
    "contentType": "id-signedData",
    "content": {
      "version": 1,
      "context": "some context",
      "messageImprint": "40f1f310cbae011642d33edbad742ea3c581864d",
      "serialNumber": 1,
      "genTime": "20191211140632Z",
      "accuracy": {
        "seconds": 0,
        "millis": 0,
        "micros": 0
      },
      "ordering": false,
      "nonce": 3455345232345454
    }
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
MIIGDjCCA/agAwIBAgIUaLzpQ6FmVRydmpKGcivf4614Z9cwDQYJKoZIhvcNAQEL
BQAwRzELMAkGA1UEBhMCVVMxETAPBgNVBAgMCE5ldyBZb3JrMREwDwYDVQQHDAhO
ZXcgWW9yazESMBAGA1UECgwJc3RhdGVkLmF0MCAXDTE5MTIxMTE0MDM1NFoYDzIy
OTMwOTI0MTQwMzU0WjBfMQswCQYDVQQGEwJVUzERMA8GA1UECAwITmV3IFlvcmsx
ETAPBgNVBAcMCE5ldyBZb3JrMRIwEAYDVQQKDAlzdGF0ZWQuYXQxFjAUBgNVBAMM
DXRzYS5zdGF0ZWQuYXQwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQC/
Mvwj1EcgG8W8of8Ic6oRr2lvV1NWs4EMCWwY+LajGi21ccmAvhz5gEv9zKVQowez
+BrFsiJEmWDlb5PCWe/Ahce6iNRukH128a+c2n8bhat0aNDzYODFF0VgD2y63DI3
3a4s6ktwwr2QF/aDq0e8TMykwHJmTv1xjmcptUH5HJic9R6WbIhEsYggCN28T0B9
owbJu754vvKyp4ykpBR9uIIg63mhJ0hg5bWbT0+ADq+UKpsbPFtBxOKx+AddXBxf
WfUDelEydxmxREeJjT1F8FAD/Ip8Cqbm3OWUAMxRe2Fdj0oxSfZAr9dB7zSRGArX
tpEl0hP/lDoBgdWV9HE+gaWnDAAE64gxpsRm2thdQAx5Q6fLqnKpXFdbkn4kTT46
UfB6rQ7SF50mHXj9T0o6KCmD5c3l7OVBe5JJZYpVry3OWV0w5zF8Ixc/VzfDCs8g
t/EkycQWrU/vm73oUB5StLcCt8XGsNM0iNuYEXfRiu4JgRICNcjkt8rYNRM/A4KE
KWGfp5PWN8jrp9L6wAnBnxUgelHUnjO5B7sbK6nqWhXCzfggjEtbdLVqFSWSk/gT
2KDsDfa9EmXEA4KeRLFrHII4BNQgyTzOsm/kcMqBoY8rhAPuTUW1pQXGtw69/aMa
J7bOkdrDFdiTjDHF9uoPsLo3T+vokTYbCP8aLjYBKQIDAQABo4HXMIHUMAkGA1Ud
EwQCMAAwCwYDVR0PBAQDAgbAMBYGA1UdJQEB/wQMMAoGCCsGAQUFBwMIMB0GA1Ud
DgQWBBQYcBR3Kv0Xfu/CHp074NupaYAF3zCBggYDVR0jBHsweYAU8Du1YgtCY+eR
KBHqNM4FApTVQNihS6RJMEcxCzAJBgNVBAYTAlVTMREwDwYDVQQIDAhOZXcgWW9y
azERMA8GA1UEBwwITmV3IFlvcmsxEjAQBgNVBAoMCXN0YXRlZC5hdIIUIrTJCIGK
X4XxEkdJfTroPXavz8IwDQYJKoZIhvcNAQELBQADggIBAMJ6jPnxHWFpSN9Wh7c6
Yn/ROOfEvfLNmrtz8VLbyhpomAhJpYOoC+7XCQxjfV+gIGpWwme5yLENf+Td/UrX
hmbzE8UzG8dmoc7/ZL825+5NrbAzYY65pdU+zj2tdmP1VFoKJ7OyjBKI4fKSZmu1
VmTa1ZQNyRu1zLIKWUXUup8ch933zxjw5tcxcsAqyKgFcDVVEw7dVSfZZf5ZUOsb
NdNZNJHIxiL3X3+/VCNp6zP2S1WvScE2dGEOq93tcGP53Z0JrmCgf4AO0jKa9xDo
+czGaZVROPCsAxupsQEdZfBJRQuKvNizHsHgkbLNr/971BP0CI+YYrJC1zoIY5tj
FEhj2GOfCsfocxaZcNIWVqBWHaDSQxZxve13HLqK/G2IBMf0fUZkpBGI5v5En7oV
l0C9gBZnrcfXg4bntVnZP36zovjZeNNO/jDg+IVOda/aY3TJivdRZn/w/hiM8Lm8
NyRrz1V3ICX4yX5mcEk3KP2YwSo33y5kXxkAI/3iMBgA9i5wHT4v1Pp7PYLdIrPt
StraXEEO9rtlxSIAkVgQDLaWujgjPq0gTzPfLokAKYwBytS5Lvp4TPob9hXvrpQR
hWEqnNMCSax8IXe0oKhc3D/2q9H/ris2j89dbSC70qde6SZ1OIXLWnXA+Lw/D+Tu
/aMUJHf5H5quEg5yFLxLfNcK
-----END CERTIFICATE-----
```
