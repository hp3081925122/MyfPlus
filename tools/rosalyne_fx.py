"""强化罗莎琳特效贴图：在目标像素网格上逐格构造，二值透明，暮金 / 玫瑰 / 暮紫限色板。

保留不动：thorn_a、thorn_b、sprout_mark、petal_break、petal_blade、duel_ring。
用法：python tools/rosalyne_fx.py [预览目录]
"""
import math
import os
import random
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "src", "main", "resources", "assets", "myfplus", "textures", "fx", "rosalyne")

PALE = (0xFF, 0xF4, 0xD8)
GOLD_L = (0xFF, 0xDC, 0x96)
GOLD = (0xF0, 0xB0, 0x50)
GOLD_D = (0xC8, 0x78, 0x30)
EMBER = (0xE8, 0x80, 0x4A)
ROSE_L = (0xEE, 0x74, 0x8C)
ROSE = (0xC8, 0x3C, 0x5A)
ROSE_D = (0x8A, 0x22, 0x40)
VIO_L = (0xB0, 0x78, 0xD8)
VIO = (0x8A, 0x4A, 0xB8)
VIO_D = (0x4A, 0x2A, 0x70)
INK = (0x14, 0x10, 0x1E)


class C:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.px = [[None] * w for _ in range(h)]

    def set(self, x, y, c):
        x, y = int(math.floor(x)), int(math.floor(y))
        if 0 <= x < self.w and 0 <= y < self.h and c is not None:
            self.px[y][x] = c

    def get(self, x, y):
        return self.px[y][x] if 0 <= x < self.w and 0 <= y < self.h else None

    def outline(self, col, diag=False):
        add = []
        nb = [(1, 0), (-1, 0), (0, 1), (0, -1)] + ([(1, 1), (1, -1), (-1, 1), (-1, -1)] if diag else [])
        for y in range(self.h):
            for x in range(self.w):
                if self.px[y][x] is None and any(self.get(x + dx, y + dy) not in (None, col) for dx, dy in nb):
                    add.append((x, y))
        for x, y in add:
            self.px[y][x] = col


def petal(c, cx, cy, ang, length, width, body, light, tip=None):
    ca, sa = math.cos(ang), math.sin(ang)
    r = int(length) + 2
    for y in range(int(cy) - r, int(cy) + r + 1):
        for x in range(int(cx) - r, int(cx) + r + 1):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            u = dx * ca + dy * sa
            v = -dx * sa + dy * ca
            if u < 0 or u > length:
                continue
            k = u / length
            half = width / 2 * math.sin(math.pi * min(1.0, k * 1.15)) if k < 0.87 else width / 2 * (1 - k) * 3
            if abs(v) <= max(0.45, half):
                col = body
                if v < -half * 0.1 and k < 0.75:
                    col = light
                if tip and k > 0.8:
                    col = tip
                c.set(x, y, col)


def stack(frames, w, h):
    img = Image.new("RGBA", (w, h * len(frames)), (0, 0, 0, 0))
    for i, c in enumerate(frames):
        for y in range(h):
            for x in range(w):
                if c.px[y][x] is not None:
                    img.putpixel((x, i * h + y), c.px[y][x] + (255,))
    return img


def sunset():
    out = []
    for f in range(4):
        c = C(64, 64)
        cx = cy = 31.5
        rot = f * math.pi / 16
        for y in range(64):
            for x in range(64):
                dx, dy = x + 0.5 - 32, y + 0.5 - 32
                r = math.hypot(dx, dy)
                th = math.atan2(dy, dx) - rot
                if r <= 12.5:
                    col = PALE if r < 5.5 else GOLD_L if r < 8.5 else GOLD if r < 11 else EMBER
                    c.set(x, y, col)
                    continue
                if 15 <= r < 16.2 and (x + y) % 2 == 0:
                    c.set(x, y, ROSE_L)
                    continue
                d8 = abs((th / (math.pi / 4)) - round(th / (math.pi / 4))) * (math.pi / 4) * r
                dmid = abs(((th + math.pi / 8) / (math.pi / 4)) - round((th + math.pi / 8) / (math.pi / 4))) * (math.pi / 4) * r
                if 14 <= r <= 31:
                    w = 2.2 * (1 - (r - 14) / 18)
                    if d8 <= max(0.5, w):
                        c.set(x, y, PALE if d8 < w * 0.35 and r < 22 else GOLD if r < 25 else GOLD_D)
                        continue
                if 14 <= r <= 23:
                    w = 1.4 * (1 - (r - 14) / 10)
                    if dmid <= max(0.45, w):
                        c.set(x, y, ROSE_L if r < 19 else ROSE)
        for y in range(64):
            for x in range(64):
                r = math.hypot(x + 0.5 - 32, y + 0.5 - 32)
                if 12.5 < r <= 13.6 and c.px[y][x] is None:
                    c.px[y][x] = ROSE_D
        out.append(c)
    return stack(out, 64, 64)


def sun_column():
    W, H = 16, 48
    streaks = [(0, 34, 2), (2, 44, 6), (5, 26, 0), (7, 40, 4), (10, 30, 7), (12, 46, 3), (14, 22, 5)]

    def tone(y):
        k = 1 - y / H
        return PALE if k < 0.18 else GOLD_L if k < 0.42 else GOLD if k < 0.66 else ROSE_L if k < 0.85 else ROSE

    out = []
    for f in range(4):
        rnd = random.Random(101 + f * 13)
        c = C(W, H)
        drop = [0.0, 0.1, 0.32, 0.58][f]
        for x, length, ph in streaks:
            top = H - length
            for y in range(top, H):
                if (y + f * 6 + ph) % 10 >= 8:
                    continue
                if rnd.random() < drop * (1.4 - (y - top) / max(1, length)):
                    continue
                c.set(x, y, tone(y))
        for base in (38 - f * 10, 14 - f * 10 + 48):
            y = base % H
            if y < 6:
                continue
            for x in range(W):
                if f >= 2 and (x + f) % 3 == 0:
                    continue
                c.set(x, y, GOLD_L)
        for i in range(5):
            px = (i * 7 + f * 3) % W
            py = (40 - i * 9 - f * 7) % H
            c.set(px, py, ROSE)
            c.set((px + 1) % W, py, ROSE_L)
        for y in range(H - [3, 2, 1, 0][f], H):
            for x in range(W):
                if f == 0 or (x + y) % 2 == 0:
                    c.set(x, y, GOLD_L if y < H - 1 else GOLD)
        out.append(c)
    return stack(out, W, H)


def slash_arc():
    W, H = 64, 12
    layers = [PALE, PALE, GOLD_L, GOLD_L, GOLD, GOLD, EMBER, ROSE_L, ROSE, ROSE, VIO, VIO_D]
    out = []
    for f in range(6):
        rnd = random.Random(7 + f * 19)
        c = C(W, H)
        scale = [1.0, 1.0, 0.85, 0.65, 0.45, 0.3][f]
        drop = [0.0, 0.0, 0.08, 0.25, 0.45, 0.7][f]
        for x in range(W):
            u = (x + 0.5) / W
            t = H * math.sin(math.pi * u) ** 0.7 * scale
            for y in range(H):
                if y >= t:
                    continue
                depth = y / max(1.0, t)
                if rnd.random() < drop * (0.4 + depth):
                    continue
                c.set(x, y, layers[min(len(layers) - 1, int(depth * len(layers)))])
        if f >= 3:
            for i in range(6 + f):
                c.set(rnd.randrange(W), rnd.randrange(H), ROSE_L if i % 2 else VIO_L)
        out.append(c)
    return stack(out, W, H)


def thrust():
    W, H = 48, 10
    out = []
    for f in range(4):
        rnd = random.Random(31 + f * 7)
        c = C(W, H)
        cut = [0.0, 0.2, 0.45, 0.7][f]
        for x in range(W):
            u = (x + 0.5) / W
            if u < cut:
                continue
            half = 4.6 * (u ** 0.55) if u < 0.82 else 4.6 * 0.9 * (1 - u) / 0.18
            for y in range(H):
                v = abs(y + 0.5 - H / 2)
                if v > half:
                    continue
                k = v / max(0.6, half)
                col = PALE if k < 0.3 else GOLD_L if k < 0.55 else GOLD if k < 0.8 else ROSE
                if u < 0.3:
                    col = ROSE_L if k < 0.5 else VIO
                    if rnd.random() < 0.35 + f * 0.15:
                        continue
                c.set(x, y, col)
        for i in range(3 + f * 2):
            x = rnd.randrange(int(W * cut), max(int(W * cut) + 1, W // 2))
            c.set(x, rnd.randrange(H), ROSE_L if i % 2 else VIO_L)
        out.append(c)
    return stack(out, W, H)


def glint():
    out = []
    for f in range(4):
        c = C(16, 16)
        arm = [3, 7, 6, 3][f]
        diag = [0, 2, 3, 0][f]
        for i in range(-arm, arm + 1):
            col = PALE if abs(i) < 2 else GOLD_L if abs(i) < arm - 1 else ROSE_L
            c.set(8 + i, 8, col)
            c.set(8, 8 + i, col)
            if f == 1 and abs(i) < 2:
                c.set(7 + i, 7, GOLD_L)
                c.set(7, 7 + i, GOLD_L)
        for i in range(1, diag + 1):
            for sx, sy in ((1, 1), (1, -1), (-1, 1), (-1, -1)):
                c.set(8 + sx * i, 8 + sy * i, GOLD if i < diag else ROSE)
        c.set(8, 8, PALE if f < 3 else GOLD_L)
        out.append(c)
    return stack(out, 16, 16)


def sigil_parts(c, f, push=0.0, keep=1.0, rnd=None):
    cx = cy = c.w / 2
    rot = f * math.pi / 10
    for y in range(c.h):
        for x in range(c.w):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            r = math.hypot(dx, dy)
            th = math.atan2(dy, dx)
            seg = int(((th + math.pi) / (2 * math.pi)) * 8) % 8
            if rnd is not None and rnd.random() > keep:
                continue
            p = push * (0.6 + 0.4 * ((seg * 5) % 8) / 8)
            if 19.5 + p <= r < 21.5 + p:
                c.set(x, y, GOLD_L if r < 20.5 + p else GOLD)
            elif 15.5 + p * 0.6 <= r < 16.5 + p * 0.6 and int((th + rot) / (math.pi / 12)) % 2 == 0:
                c.set(x, y, VIO_L)
    for k in range(4):
        a = k * math.pi / 2 + math.pi / 4
        rr = 21.5 + push
        for s in range(5):
            w = (4 - s) * 0.5
            for t in (-w, 0, w):
                px = cx + math.cos(a) * (rr + s) - math.sin(a) * t
                py = cy + math.sin(a) * (rr + s) + math.cos(a) * t
                if rnd is None or rnd.random() <= keep:
                    c.set(px, py, ROSE if s < 3 else ROSE_L)


def flower(c, cx, cy, rot, r, spread=0.0, keep=1.0, rnd=None):
    for k in range(5):
        a = rot + k * 2 * math.pi / 5
        if rnd is not None and rnd.random() > keep:
            continue
        ox, oy = math.cos(a) * spread, math.sin(a) * spread
        petal(c, cx + ox, cy + oy, a, r, r * 0.9, ROSE, ROSE_L, ROSE_D if spread == 0 else None)
    if spread < 2:
        for y in range(-2, 3):
            for x in range(-2, 3):
                if x * x + y * y <= 4:
                    c.set(cx + x, cy + y, GOLD_L if x * x + y * y <= 1 else GOLD)


def parry_sigil():
    out = []
    for f in range(4):
        c = C(48, 48)
        sigil_parts(c, f)
        flower(c, 24, 24, f * (2 * math.pi / 5) / 4 - math.pi / 2, 9)
        c.outline(INK)
        out.append(c)
    return stack(out, 48, 48)


def parry_break():
    out = []
    for f in range(5):
        rnd = random.Random(53 + f)
        c = C(64, 64)
        push = [0, 2, 4, 6, 7][f]
        keep = [1.0, 0.9, 0.6, 0.35, 0.15][f]
        sigil_parts(c, 0, push, keep, rnd)
        if f == 0:
            flower(c, 32, 32, -math.pi / 2, 9)
            for k in range(6):
                a = k * math.pi / 3 + 0.3
                for s in range(2, 16):
                    c.set(32 + math.cos(a) * s, 32 + math.sin(a) * s, PALE)
        else:
            flower(c, 32, 32, -math.pi / 2 + f * 0.5, max(3, 9 - f * 1.5), spread=push * 0.8, keep=keep + 0.2, rnd=rnd)
        if f < 3:
            c.outline(INK)
        out.append(c)
    return stack(out, 64, 64)


def petal_burst():
    out = []
    for f in range(5):
        rnd = random.Random(77 + f)
        c = C(48, 48)
        dist = [3, 8, 13, 17, 20][f]
        size = [6, 7, 6, 4, 2.5][f]
        for k in range(10):
            a = k * 2 * math.pi / 10 + f * 0.25 + (k % 2) * 0.15
            d = dist * (1.0 if k % 2 == 0 else 0.8)
            px, py = 24 + math.cos(a) * d, 24 + math.sin(a) * d
            if f >= 3 and rnd.random() < 0.3:
                continue
            body, light = (ROSE, ROSE_L) if k % 3 else (VIO, VIO_L)
            petal(c, px, py, a + 0.6, size, size * 0.75, body, light)
        if f <= 1:
            arm = [7, 5][f]
            for i in range(-arm, arm + 1):
                col = PALE if abs(i) < 3 else GOLD_L
                c.set(24 + i, 24, col)
                c.set(24, 24 + i, col)
        for i in range(4 + f * 2):
            a = rnd.random() * 2 * math.pi
            d = dist * (0.5 + rnd.random() * 0.7)
            c.set(24 + math.cos(a) * d, 24 + math.sin(a) * d, GOLD_L if i % 2 else GOLD)
        if f < 4:
            c.outline(INK)
        out.append(c)
    return stack(out, 48, 48)


def main():
    textures = {
        "sunset": sunset(),
        "sun_column": sun_column(),
        "slash_arc": slash_arc(),
        "thrust": thrust(),
        "glint": glint(),
        "parry_sigil": parry_sigil(),
        "parry_break": parry_break(),
        "petal_burst": petal_burst(),
    }
    preview = sys.argv[1] if len(sys.argv) > 1 else None
    for name, img in textures.items():
        img.save(os.path.join(OUT, name + ".png"))
        if preview:
            s = 6
            img.resize((img.width * s, img.height * s), Image.NEAREST).save(os.path.join(preview, name + ".png"))


if __name__ == "__main__":
    main()
