# ~Pulsar Server Monitor

ci badge, mit license

img1
img2

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

**category**

|```json``` | icon |
|--|--|
|```{class: pulsar}```    | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |
|```{class: server}```    | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |
|```{class: database}```  | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |
|```{class: media}```     | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |
|```{class: messaging}``` | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |
|```{class: backup}```    | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |
|```{class: web}```       | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |
|```{class: network}```   | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |
|```{class: human}```     | ![](/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp) |

**severity**
|```json``` | color |
|--|--|
|```{sev: 0}``` | `#ff4c4c` |
|```{sev: 1}``` | `#ff864e` |
|```{sev: 3}``` | `#e5e564` |
|```{sev: 4}``` | `#7df9ff` |
|```{sev: 6}``` | `#bb86fc` |

## Scripts
Here's some ways I use this. Feel free to copy them!  
These all use [sendxmpp-rs](https://github.com/moparisthebest/sendxmpp-rs).

- **shutdown** - *[systemd](scripts/shutdown.service), [shell](scripts/shutdown.sh)*
- **tailscale** - *[systemd](scripts/tailscale.service), [shell](scripts/tailscale.sh)*