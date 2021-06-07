fun buildSystemdConfig(id: String, secret: String, redirect: String, dir: String): String {
	return """
[Unit]
Description=flotion
After=syslog.target
After=network.target[Service]
User=joe
Type=simple

[Service]
Environment="flotion_client_id=$id"
Environment="flotion_client_secret=$secret"
Environment="flotion_redirect_uri=$redirect"
ExecStart=/usr/bin/java -jar $dir/build/libs/flotion-0.0.1-SNAPSHOT.jar
Restart=always
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=flotion

[Install]
WantedBy=multi-user.target
""";
}

fun buildCaddyConfig(): String {
	return """
$FLOTION_API_URL {
    reverse_proxy 127.0.0.1:$FLOTION_API_PORT
}

$FLOTION_URL {
    reverse_proxy 127.0.0.1:$FLOTION_PORT
}
"""
}
