# Sockastic
Sockastic is a simple demonstration program written in Kotlin for the Android platform that performs
some basic socket operations. It also shows a variety of other simple constructs for handling UI operations, etc.

Sockastic has two functions: a simple socket server, and a local client, with an interactive UI.
Its server listens for network connections, and provides a simple atomic element lookup service.
Its client may be used to connect to any reachable server, or to the local Sockastic server.

It is a complete Android Studio project that may be cloned, executed and examined.  It is still being refined.

*Some small but useful examples collected here include:*

Using ServerSocket to create a server.
Accepting and handling multiple client sessions.
Processing and responding to client requests.
Using Socket to create a client.
Connecting and interacting with a server.

Using simple markup in TextView.
Context aware UI components.
Scrolling discrete TextViews within a ScrollView.
Using soft keyboard for IP address entry.
Kotlin language features.
EditText clear widget.
Long press context menu.

Sockastic operation details available in the UI DOCS dialog.

Portions of the program were inspired and informed by many fine examples from 
other developers who share their creative efforts on GitHub and elsewhere.

Demonstrates socket connection operations
Sockastic is both a simple socket server, and a local client.
The server may also be exercised by another machine on the same LAN
An example using *TELNET*:

**telnet 192.168.1.164 9985**

The client may also be used to connect to an external socket server.


