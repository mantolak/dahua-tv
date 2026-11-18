#!/usr/bin/env bash
# Fake Dahua camera on this Mac: H.265 test pattern + G.711 A-law audio over RTSP (TCP, Digest auth).
#
#   tools/fake-camera/run.sh            # 704x480 @ 20 fps (like the sub-stream)
#   tools/fake-camera/run.sh 3840x2160  # 4K (like the main stream)
#   VCODEC=libx264 tools/fake-camera/run.sh   # H.264 instead of H.265
#
# In the app (emulator) set:  IP 10.0.2.2 · port 10554 · user admin · password t3st@P#ss
# From the Mac itself:        ffplay -rtsp_transport tcp "rtsp://admin:t3st%40P%23ss@127.0.0.1:10554/cam/realmonitor"
set -euo pipefail
SIZE="${1:-704x480}"
VCODEC="${VCODEC:-libx265}"
if [ "$VCODEC" = libx265 ]; then VOPTS=(-x265-params "keyint=40:log-level=error"); else VOPTS=(-g 40); fi
DIR="$(cd "$(dirname "$0")" && pwd)"

mediamtx "$DIR/mediamtx.yml" &
MTX=$!
trap 'kill $MTX 2>/dev/null' EXIT
sleep 1

ffmpeg -hide_banner -loglevel warning -re \
  -f lavfi -i "testsrc2=size=${SIZE}:rate=20" \
  -f lavfi -i "sine=frequency=440:sample_rate=8000" \
  -c:v "$VCODEC" -preset ultrafast -tune zerolatency -pix_fmt yuv420p "${VOPTS[@]}" \
  -c:a pcm_alaw -ar 8000 -ac 1 \
  -f rtsp -rtsp_transport tcp "rtsp://127.0.0.1:10554/cam/realmonitor"
