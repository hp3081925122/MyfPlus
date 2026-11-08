"""敲钟人余音声波柱贴图：16×48 每帧，4 帧纵向排列，左右边缘可无缝环绕成圆筒。

用法：python tools/bellringer_column.py [预览图输出路径]
"""
import os
import random
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "src", "main", "resources", "assets", "myfplus", "textures", "fx", "bellringer", "echo_column.png")

W, H, FRAMES = 16, 48, 4
C0, C1, C2, C3 = (0x3A, 0x9C, 0xC8), (0x5F, 0xD8, 0xE8), (0xA8, 0xF0, 0xF8), (0xEC, 0xFE, 0xFF)
STREAKS = [(0, 30, 3), (3, 44, 0), (5, 22, 5), (8, 38, 2), (10, 26, 7), (13, 41, 1)]


def tone(y):
    k = 1 - y / H
    if k < 0.2:
        return C3
    if k < 0.5:
        return C2
    if k < 0.8:
        return C1
    return C0


def frame(f):
    rnd = random.Random(17 + f * 31)
    px = [[None] * W for _ in range(H)]
    drop = [0.0, 0.12, 0.35, 0.6][f]
    for x, length, phase in STREAKS:
        top = H - length
        for y in range(top, H):
            seg = (y + f * 5 + phase) % 9
            if seg >= 7:
                continue
            if rnd.random() < drop * (1.4 - (y - top) / max(1, length)):
                continue
            px[y][x] = tone(y)
    for base in (40 - f * 11, 18 - f * 11 + 44):
        y = base % H
        if y < 4:
            continue
        for x in range(W):
            if f >= 2 and (x + f) % 4 == 0:
                continue
            px[y][x] = C3 if y < H - 6 else C2
            if y + 1 < H:
                px[y + 1][x] = C1
    foot = [3, 2, 1, 0][f]
    for y in range(H - foot, H):
        for x in range(W):
            if f == 0 or (x + y) % 2 == 0:
                px[y][x] = C2 if y < H - 1 else C1
    return px


def main():
    img = Image.new("RGBA", (W, H * FRAMES), (0, 0, 0, 0))
    for f in range(FRAMES):
        px = frame(f)
        for y in range(H):
            for x in range(W):
                if px[y][x] is not None:
                    img.putpixel((x, f * H + y), px[y][x] + (255,))
    img.save(OUT)
    if len(sys.argv) > 1:
        img.resize((W * 8, H * FRAMES * 8), Image.NEAREST).save(sys.argv[1])


if __name__ == "__main__":
    main()
