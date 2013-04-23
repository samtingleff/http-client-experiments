#include <stddef.h>

#define RUBICON_HTTP_METHOD_GET 1
#define RUBICON_HTTP_METHOD_POST 2

#define ERRORS_DNS 1
#define ERRORS_CONNECTION_CLOSED = 2
#define ERRORS_TIMEOUT 3
// there should be more of these

typedef struct
{
	size_t len;
	unsigned char data[];
} bytestring;

typedef struct
{
	// name ("Content-Type")
	bytestring name;

	// value ("application/json")
	bytestring value;
} http_header;

typedef struct
{
	size_t len;
	http_header headers[];
} http_header_list;

typedef struct
{
	// unique identifier for this request object
	// provided back to the caller in http_client_response
	int request_id;

	// destination URL
	bytestring url;

	// HTTP method (GET/POST; see above defines)
	int method;

	// request (POST) body, if there is one
	bytestring request_body;

	// request headers, if any
	http_header_list headers;
} http_client_request;

typedef struct
{
	size_t len;
	http_client_request requests[];
} http_client_request_list;

typedef struct
{
	// overall millisecond timeout to return to the caller
	int timeout;

	// list of requests
	http_client_request_list requests;
} http_client_multirequest;

typedef struct
{
	// unique identifier from http_client_request
	int request_id;

	// all possible statuses that do not result in a valid http response
	// from the destination URL.
	// see ERRORS_DNS, etc defined above
	int request_status;

	// response time in milliseconds for this request
	int response_time;

	// http status code returned
	int http_response_code;

	// response body
	bytestring request_body;

	// response headers
	http_header_list headers;
} http_client_response;

typedef struct
{
	size_t len;
	http_client_response responses[];
} http_client_response_list;

typedef struct
{
	// total time in millis spent on this request
	int response_time;

	// all responses
	http_client_response_list responses;
} http_client_multiresponse;

/**
 * Submit requests, receive responses.
 */
http_client_multiresponse request(http_client_multirequest requests);
