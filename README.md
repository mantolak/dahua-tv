# Dahua TV

Android TV app that shows 4 Dahua IP cameras in a 2x2 grid on the local network.
Camera credentials are stored on the device (encrypted), never in the repo.

## Stream URL format (Dahua)
rtsp://USER:PASS@IP:554/cam/realmonitor?channel=1&subtype=1   (sub-stream, used for grid)
rtsp://USER:PASS@IP:554/cam/realmonitor?channel=1&subtype=0   (main stream, fullscreen)
