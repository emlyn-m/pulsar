# ~Pulsar Server Monitor

<div style="align:center;">
<img src="https://github.com/emlyn-m/pulsar/actions/workflows/android.yml/badge.svg" />
</div>

<!-- TO ADD:
--Row 1-- 
CI badge,  MIT License icon

-- Row 2 --
screenshot of home screen
-->

## Server Configuration
This tool requires an XMPP server to send and receive messages, and the scripts provided are designed for `systemd`.

### Message Format
```
message {
    sev: number,
    class: string,
    timestamp: number, // unix time, seconds
    body: string
}
```

## Configuration

**local.properties**
```
XMPP_ADDR="prosody.example.com"
XMPP_USER="test_user"
XMPP_PASS="test_password"
XMPP_PORT=5222
```

<table>
<tr>
<th align="right">
<img width="441" height="1">
    <b>Category</b>
</th>
<th align="left">
<img width="441" height="1">
    <b>Severity</b>
</th>
</tr>
<tr>
<td align="right">

<table>
    <tr>
        <th><code>json</code></th>
        <th>icon</th>
    </tr>
    <tr>
        <td><code>{class: "pulsar"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/pulsar.svg" /></td>
    </tr>
        <tr>
        <td><code>{class: "server"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/server.svg" /></td>
    </tr>
    <tr>
        <td><code>{class: "database"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/db.svg" /></td>
    </tr>
    <tr>
        <td><code>{class: "media"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/media.svg" /></td>
    </tr>
    <tr>
        <td><code>{class: "messaging"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/msg.svg" /></td>
    </tr>
    <tr>
        <td><code>{class: "backup"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/backup.svg" /></td>
    </tr>
    <tr>
        <td><code>{class: "web"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/web.svg" /></td>
    </tr>
    <tr>
        <td><code>{class: "network"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/net.svg" /></td>
    </tr>
    <tr>
        <td><code>{class: "human"}</code></td>
        <td><img src="/app/src/main/res/drawable/svgs/human.svg" /></td>
    </tr>


</table>
</td>
<td align="left" valign="top">

<table>
    <tr valign="bottom">
        <th align="left"><code>json</code></th>
        <th align="left">color</th>
    </tr>
    <tr>
        <td><code>{sev: 0}</code></td><td><img src="https://fpoimg.com/20x20?text=text&bg_color=ff4c4c&text_color=ff4c4c" /><code>#ff4c4c</code></td>
    </tr>
    <tr>
        <td><code>{sev: 1}</code></td><td><img src="https://fpoimg.com/20x20?text=text&bg_color=ff864e&text_color=ff864e" /><code>#ff864e</code></td>
    </tr><tr>
        <td><code>{sev: 3}</code></td><td><img src="https://fpoimg.com/20x20?text=text&bg_color=e5e564&text_color=e5e564" /><code>#e5e564</code></td>
    </tr><tr>
        <td><code>{sev: 4}</code></td><td><img src="https://fpoimg.com/20x20?text=text&bg_color=7df9ff&text_color=7df9ff" /><code>#7df9ff</code></td>
    </tr><tr>
        <td><code>{sev: 6}</code></td><td><img src="https://fpoimg.com/20x20?text=text&bg_color=bb86fc&text_color=bb86fc" /><code>#bb86fc</code></td>
    </tr>
</table>
  
</td>
</tr>
</table>

## Scripts
Here's some ways I use this. Feel free to copy them!  

- **coming soon.**
