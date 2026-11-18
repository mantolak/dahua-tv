# Dahua TV

Android TV app that shows 4 Dahua IP cameras in a 2x2 grid on the local network.
Camera credentials are stored on the device (encrypted), never in the repo.

## Stream URL format (Dahua)
rtsp://USER:PASS@IP:554/cam/realmonitor?channel=1&subtype=1   (sub-stream, used for grid)
rtsp://USER:PASS@IP:554/cam/realmonitor?channel=1&subtype=0   (main stream, fullscreen)

## Design decisions
- Cameras: 4x Dahua, H.265 (HEVC), G.711 A-law audio.
- Grid view (2x2): sub-stream `subtype=1` (704x480 @ 20 fps), audio muted.
- Select a tile with the remote -> fullscreen that camera on main stream `subtype=0` (3840x2160 @ 20 fps). Back -> return to grid.
- Player: AndroidX Media3 ExoPlayer + RTSP module (TCP transport), hardware HEVC decoding.
- Target device: Sony Android/Google TV (192.168.0.46), deploy over network adb.
- Credentials: entered on the TV, stored encrypted on device; never committed.
