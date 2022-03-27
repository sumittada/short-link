# Mini-project to demonstrate onboarding with Scala


Scala Play framework based API that creates a mapping between a given url and a short-code that can be used to provide a link shortening service.

Docker containerized deployment of this service is running at https://journi.sumit.fi for no-installation usage.

To run the service locally, ensure you have Scala and Play framework working on your machine and then to start the localhost development server run the following command:
`sbt run`
The localhost service will be accessible at http://localhost:9000 after this.

To run the tests, run the following command:
`sbt test`

Following endpoints are implemented:


- `/encode` (POST)

Accepts a url in request body, creates a short-code mapping and adds it to in-memory list for future retrieval

Sample request
```json
    {
        "url": "https://www.journiapp.com/photo-book"
    }
```
Sample response
```json
    {
        "shortCode": "TxTpcy8vgp",
        "url": "https://www.journiapp.com/photo-book"
    }
```

- `/decode/<shortCode>` (GET)

Accepts a shortCode string as part of request url and returns the original full url in response body is present, http 404 otherwise

Sample url: `https://journi.sumit.fi/decode/TxTpcy8vgp`

Sample response
```json
    {
        "shortCode": "TxTpcy8vgp",
        "url": "https://www.journiapp.com/photo-book"
    }
```


- `/all` (GET)

Testing helper endpoint that sends a list of all existing url-shortCode mapping


Sample response
```json
    [
        {
            "shortCode": "DummYShort",
            "url": "https://journiapp.com"
        },
        {
            "shortCode": "ShoRTdummY",
            "url": "https://bambus.io"
        },
        {
            "shortCode": "D493uPDyTz",
            "url": "https://www.journiapp.com/photo-book"
        },
        {
            "shortCode": "otLx2LInJP",
            "url": "https://sumit.fi/another-sample-url/"
        }
    ]
```


- ` /go/<shortCode>` (GET)

Sample url: `https://journi.sumit.fi/go/TxTpcy8vgp`
Short-link to original url redirect endpoint. If the mapping exists for given shortcode, it directly redirects the user to the original url, no response body. Intended to provide an example short-url-service that can be used by end users directly instead of as an REST API endpoint.

