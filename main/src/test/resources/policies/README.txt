The "input" prefix indicates that this is what the payload looks like on the POST. While the "output" prefix
indicates that this is what the payload looks like when the response goes back to the caller.

For each of the use cases, there is at least one configuration policy for DCAE Microservice. 

*.monitoring.input.[json|yaml] <-- POST request

*.monitoring.output.json --> POST response

The Operational Policies:

*.operational.input.yaml --> Can Pam change this to JSON??

*.operational.output.json --> POST response

The Guard Policies:

*.guard.[frequency|minmax].json <-- POST request

*.guard.[frequency|minmax].json --> POST response