3|MONITOR|ICMPMonitor|0,11,22,33,44,55 */2 * * * ? *|{ "host": "127.{{OCTET_HIGH}}.{{OCTET_MID}}.{{OCTET_LOW}}" }|
3|MONITOR|ICMPMonitor|5000|{ "host": "127.{{OCTET_HIGH}}.{{OCTET_MID}}.{{OCTET_LOW}}" }|
1|LISTENER|WebListener||{ "address": "0.0.0.0", "port": "9999" }|
1|LISTENER|WebListener||{ "address": "0.0.0.0", "port": "9998" }|
1|CONNECTOR|TestTcpConnector||{ "host": "poc-test-driver", "port": "9980" }|
