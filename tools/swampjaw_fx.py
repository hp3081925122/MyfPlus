"""强化沼牙特效贴图：直接在目标像素网格上逐格构造，二值透明、按材质描边。

用法：python tools/swampjaw_fx.py [预览图输出路径]
"""
import math
import os
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
TEX = os.path.join(HERE, "..", "src", "main", "resources", "assets", "myfplus", "textures")

P = {
    "O": (42, 33, 22),
    "b0": (142, 122, 85), "b1": (201, 185, 142), "b2": (237, 227, 196), "b3": (255, 248, 230),
    "w0": (31, 59, 54), "w1": (59, 110, 98), "w2": (94, 156, 134), "w3": (158, 210, 184), "w4": (230, 246, 238),
    "m0": (78, 107, 46), "m1": (126, 156, 66),
    "a0": (199, 122, 30), "a1": (242, 179, 61), "a2": (255, 224, 138),
    "e0": (58, 42, 28), "e1": (110, 82, 54), "e2": (146, 112, 74),
}
WATER = {"w1", "w2", "w3", "w4"}
ENERGY = {"a1", "a2"}


class C:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.px = [[None] * w for _ in range(h)]

    def set(self, x, y, c):
        x, y = int(math.floor(x)), int(math.floor(y))
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = c

    def get(self, x, y):
        return self.px[y][x] if 0 <= x < self.w and 0 <= y < self.h else None

    def fill(self, f):
        for y in range(self.h):
            for x in range(self.w):
                c = f(x + 0.5, y + 0.5)
                if c:
                    self.px[y][x] = c

    def line(self, x0, y0, x1, y1, c):
        n = int(max(abs(x1 - x0), abs(y1 - y0))) + 1
        for i in range(n + 1):
            t = i / n
            self.set(x0 + (x1 - x0) * t + 0.5, y0 + (y1 - y0) * t + 0.5, c)

    def outline(self, skip=()):
        """空像素挨着实体像素时描边：挨着土/骨用深褐，挨着水用深青，挨着能量用暗琥珀。"""
        add = []
        for y in range(self.h):
            for x in range(self.w):
                if self.px[y][x] is not None:
                    continue
                ns = [self.get(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
                ns = [n for n in ns if n is not None and n not in ("O", "w0", "a0") and n not in skip]
                if not ns:
                    continue
                if all(n in WATER for n in ns):
                    add.append((x, y, "w0"))
                elif all(n in ENERGY for n in ns):
                    add.append((x, y, "a0"))
                else:
                    add.append((x, y, "O"))
        for x, y, c in add:
            self.px[y][x] = c

    def image(self):
        im = Image.new("RGBA", (self.w, self.h), (0, 0, 0, 0))
        for y in range(self.h):
            for x in range(self.w):
                c = self.px[y][x]
                if c:
                    im.putpixel((x, y), P[c] + (255,))
        return im


def stack(frames):
    w, h = frames[0].w, frames[0].h
    im = Image.new("RGBA", (w, h * len(frames)), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        im.paste(f.image(), (0, h * i))
    return im


def adiff(a, b):
    return (a - b + 180) % 360 - 180


# ---------- 化石獠牙（竖直贴片 1×2 格）----------
def fang():
    c = C(16, 32)

    def f(px, py):
        t = (py - 1) / 28
        if t < 0 or t > 1:
            return None
        w = 0.6 + 5.2 * t ** 0.8
        cx = 8 + 1.8 * (1 - t) ** 2 - 0.3
        k = (px - cx) / w
        if abs(k) > 1:
            return None
        col = "b2" if k < -0.45 else ("b0" if k > 0.35 else "b1")
        if -0.62 < k < -0.38 and 6 < py < 21:
            col = "b3"
        if int(py) % 7 == 3 and t > 0.3 and abs(k) < 0.85:
            col = {"b3": "b2", "b2": "b1", "b1": "b0", "b0": "e1"}[col]
        return col

    c.fill(f)
    c.line(9, 12, 10, 16, "b0")

    def dirt(px, py):
        d = ((px - 8) / 7.6) ** 2 + ((py - 30.5) / 2.6) ** 2
        if d > 1 or py < 27:
            return None
        if py < 28.5 and (int(px) in (3, 4, 11, 12)):
            return "m1"
        return "e1" if py < 30 else "e0"

    c.fill(dirt)
    c.outline()
    return c.image()


# ---------- 獠牙破土点（地面贴花 1 格）----------
def fang_mark():
    c = C(16, 16)
    rays = (15, 85, 160, 220, 300)

    def f(px, py):
        dx, dy = px - 8, py - 8
        r = math.hypot(dx, dy)
        a = math.degrees(math.atan2(dy, dx))
        if r < 2.2:
            return "O"
        if r < 3.4:
            return "e0"
        for ra in rays:
            off = math.radians(abs(adiff(a, ra))) * r
            if r < 6.6 and off < 0.6:
                return "O"
            if r < 6.0 and off < 1.3:
                return "e1"
        if 6.4 < r < 7.6 and int((a + 180) // 30) % 2 == 0:
            return "b2"
        return None

    c.fill(f)
    c.outline()
    return c.image()


# ---------- 潜地背鳍（竖直贴片，2 帧）----------
def fin():
    frames = []
    clods = [[(1, 10), (14, 9), (3, 7), (12, 6)], [(0, 8), (13, 11), (15, 6), (2, 11)]]
    poly = [(13, 13.5), (11, 7), (8.5, 3), (5.5, 1), (6.2, 5), (5, 9.5), (3, 13.5)]
    for fi in range(2):
        c = C(16, 16)

        def inside(px, py):
            hit = False
            j = len(poly) - 1
            for i in range(len(poly)):
                xi, yi = poly[i]
                xj, yj = poly[j]
                if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi) + xi:
                    hit = not hit
                j = i
            return hit

        def f(px, py):
            if not inside(px, py):
                return None
            if not inside(px + 1, py):
                return "b2"
            for bx in (5.5, 8.5, 11.5):
                t = (13.5 - py) / 12.5
                rx = bx + (6 - bx) * t
                if abs(px - rx) < 0.55:
                    return "b1"
            return "w1" if py < 9 else "w0"

        c.fill(f)

        def mound(px, py):
            d = ((px - 8) / 7.4) ** 2 + ((py - 15.6) / 2.4) ** 2
            if d > 1:
                return None
            return "e1" if py < 14.5 else "e0"

        c.fill(mound)
        for x, y in clods[fi]:
            c.set(x, y, "e2")
            c.set(x, y + 1, "e1")
        c.outline()
        frames.append(c)
    return stack(frames)


# ---------- 骨鳍刃（面向镜头，4 帧，四臂对称，每帧转 22.5°）----------
def fin_blade():
    frames = []
    for fi in range(4):
        c = C(16, 16)
        rot = fi * 22.5

        def f(px, py):
            dx, dy = px - 8, py - 8
            r = math.hypot(dx, dy)
            a = math.degrees(math.atan2(dy, dx)) - rot
            if r < 1.0:
                return None
            if r < 2.7:
                return "b2" if dx + dy < -1 else "b1"
            if r > 7.6:
                return None
            for k in range(4):
                ac = 90 * k + (r - 2) * 14
                off = math.radians(adiff(a, ac)) * r
                lw = 1.7 * (1 - (r - 2) / 6.2) + 0.35
                if abs(off) < lw:
                    if off > lw * 0.35:
                        return "b3" if r < 5 else "b2"
                    if off < -lw * 0.4:
                        return "b0"
                    return "b1"
            return None

        c.fill(f)
        c.outline()
        frames.append(c)
    return stack(frames)


# ---------- 水柱公用 ----------
def column(c, cx, base, height, hw0, crest, lobes=0.55):
    def f(px, py):
        v = base - py
        if v < 0 or height <= 0:
            return None
        top = height - crest * abs(math.sin((px - cx) * lobes))
        if v > top:
            return None
        hw = hw0 * (1 - 0.45 * min(1, v / max(1, height)))
        k = (px - cx) / hw
        if abs(k) > 1:
            return None
        if v > top - 1.5:
            return "w4"
        if -0.6 < k < -0.35:
            return "w4" if int(py) % 3 else "w3"
        if k < -0.1:
            return "w3"
        return "w1" if k > 0.5 else "w2"

    c.fill(f)


def clod(c, x, y, s):
    for i in range(s):
        for j in range(s):
            c.set(x + i, y + j, "e1")
    c.set(x, y, "e2")


# ---------- 破土（竖直贴片 2.5×3.75 格，6 帧）----------
def breach():
    frames = []
    heights = [8, 20, 30, 33, 24, 12]
    chunks = [(-7, 7, 2), (6, 8, 2), (-4, 9, 1), (8, 6, 1), (-9, 5, 1), (3, 9.5, 2), (-1, 6, 1)]
    for fi in range(6):
        c = C(24, 36)
        column(c, 12, 34, heights[fi], [6, 6, 5.5, 5, 5, 6][fi], 0 if fi == 0 else 3)
        if fi >= 4:
            cut = (fi - 3) * 9

            def hollow(px, py):
                return None

            for y in range(c.h):
                v = 34 - (y + 0.5)
                if 2 < v < cut:
                    for x in range(c.w):
                        if c.px[y][x] in WATER and abs(x + 0.5 - 12) > 1.5:
                            c.px[y][x] = None

        def base(px, py):
            d = ((px - 12) / 10.5) ** 2 + ((py - 35.2) / 3) ** 2
            if d > 1:
                return None
            if py < 33:
                return "w4"
            return "e1" if py < 34.5 else "e0"

        c.fill(base)
        t = fi + 1
        for dx, vy, s in chunks:
            x = 12 + dx * t * 0.32
            y = 33 - (vy * t * 0.9 - 0.75 * t * t)
            if y < 33:
                clod(c, x, y, s)
        c.outline()
        frames.append(c)
    return stack(frames)


# ---------- 落点阴影（地面贴花，4 种尺寸，像素大小不变）----------
def drop_mark(n):
    c = C(n, n)
    R = n / 2

    def f(px, py):
        dx, dy = px - R, py - R
        d = math.hypot(dx, dy)
        if d >= R - 0.3:
            return None
        if d > R - 1.5:
            return "O"
        a = (math.degrees(math.atan2(dy, dx)) + 22.5) % 45
        frac = abs(a - 22.5) / 22.5
        L = max(1.6, R * 0.3)
        if d > R - 1.5 - L * frac:
            return "b2" if dy < 0 else "b1"
        return "w1" if (int(px) + int(py)) % 4 == 0 else "w0"

    c.fill(f)
    return c.image()


# ---------- 化石巨颌咬合（面向镜头 2×2 格，4 帧）----------
def maw():
    frames = []
    gaps = [10, 5, 0, 1]
    for fi, g in enumerate(gaps):
        c = C(32, 32)
        ub = 16 - g / 2
        lt = 16 + g / 2

        def f(px, py):
            ax = abs(px - 16)
            if py < ub:
                v = ub - py
                if v > 11:
                    return None
                hw = 14 - v * 0.55 - (2.5 if v > 9 else 0)
                if v > 9.2 and 3 < ax < 6.5:
                    hw = 14
                if ax > hw:
                    return None
                if ax > hw - 1.2 or v < 1.6:
                    return "b0"
                if v > 7.5 and ax < 7:
                    return "b2"
                return "b2" if px < 12 and v > 3 else "b1"
            if py > lt:
                v = py - lt
                if v > 6.5:
                    return None
                hw = 12.5 - v * 1.25
                if ax > hw:
                    return None
                return "b1" if v < 2 else ("e2" if ax < 2 and v > 4.5 else "b0")
            if abs(px - 16) < 10:
                return "w0"
            return None

        c.fill(f)
        for xc, h in ((5, 3), (8, 5), (11, 3), (14, 3), (18, 3), (21, 3), (24, 5), (27, 3)):
            for y in range(32):
                py = y + 0.5
                if ub <= py <= ub + h:
                    hw = 1.4 * (1 - (py - ub) / h) + 0.3
                    for x in range(32):
                        if abs(x + 0.5 - xc) < hw:
                            c.px[y][x] = "b3" if x + 0.5 < xc else "b2"
        for xc, h in ((7, 3), (10, 4), (13, 3), (19, 3), (22, 4), (25, 3)):
            for y in range(32):
                py = y + 0.5
                if lt - h <= py <= lt:
                    hw = 1.4 * (1 - (lt - py) / h) + 0.3
                    for x in range(32):
                        if abs(x + 0.5 - xc) < hw:
                            c.px[y][x] = "b2"
        ey = int(ub - 6)
        for ex in (9, 21):
            for i in range(3):
                for j in range(2):
                    c.set(ex + i, ey + j, "O")
            c.set(ex + 1, ey, "a1")
        c.set(15, ub - 3, "O")
        c.set(17, ub - 3, "O")
        c.line(6, ub - 4, 8, ub - 2, "b0")
        c.line(24, ub - 8, 26, ub - 6, "b0")
        c.outline()
        if fi >= 2:
            ticks = [(0, 16, 3, 16), (1, 11, 3, 13), (1, 21, 3, 19), (29, 16, 31, 16), (29, 13, 31, 11), (29, 19, 31, 21)]
            for x0, y0, x1, y1 in ticks[: 6 if fi == 2 else 2] + (ticks[3:5] if fi == 3 else []):
                c.line(x0, y0, x1, y1, "w4")
        frames.append(c)
    return stack(frames)


# ---------- 吐息蓄力（嘴前，4 帧循环）----------
def charge():
    frames = []
    for fi in range(4):
        c = C(16, 16)
        core = 2 + fi * 0.45

        def f(px, py):
            d = math.hypot(px - 8, py - 8)
            if d < core * 0.5:
                return "w4"
            if d < core:
                return "w3" if px + py < 16 else "w2"
            return None

        c.fill(f)
        r = 7 - 1.5 * fi
        for k in range(6):
            a = math.radians(k * 60 + fi * 18)
            x, y = 8 + math.cos(a) * r - 0.5, 8 + math.sin(a) * r - 0.5
            c.set(x, y, "w3")
            c.set(x + 1, y, "w2")
            c.set(x, y + 1, "w2")
        c.outline()
        frames.append(c)
    return stack(frames)


# ---------- 化石骨刺（竖直贴片 1×2 格，尖端朝下，3 个变体按 count 取帧）----------
def bone_spike():
    frames = []
    for hw, lean, top, seg in ((3.6, 0.0, 3, 4), (3.0, 1.6, 5, 2), (4.0, -1.4, 2, 5)):
        c = C(16, 32)

        def f(px, py, hw=hw, lean=lean, top=top, seg=seg):
            if py < top or py > 31:
                return None
            t = (py - top) / (31 - top)
            cx = 8 + lean * (t - 0.5)
            w = hw * (1 - t ** 1.25) + 0.45
            if t < 0.1:
                w *= 0.8 + t * 2
            dx = px - cx
            if abs(dx) > w:
                return None
            rel = dx / w
            if t < 0.06 and int(px) % 3 == 0:
                return None
            if int(py - top) % 7 == seg and abs(rel) < 0.8 and t < 0.8:
                return "b0"
            if rel < -0.45:
                return "b3" if t < 0.45 else "b2"
            if rel > 0.4:
                return "b0"
            return "b2" if rel < -0.1 and t < 0.6 else "b1"

        c.fill(f)
        for x in range(16):
            if c.get(x, top):
                c.set(x, top, "b0")
        c.outline()
        frames.append(c)
    return stack(frames)


# ---------- 落点水花（竖直贴片，5 帧）----------
def spout():
    frames = []
    for fi, h in enumerate([5, 12, 18, 15, 8]):
        c = C(12, 24)
        column(c, 6, 23, h, 4.2 if fi < 3 else 4.8, 0 if fi == 0 else 2, 0.9)
        if fi >= 2:
            for x, y in ((2, 23 - h - 2), (9, 23 - h - 1), (5, 23 - h - 4 + fi)):
                c.set(x, y, "w4")
        c.outline()
        frames.append(c)
    return stack(frames)


# ---------- 尾击弧带（弧长 48 × 径向 8，4 帧；每帧第 0 行为外缘）----------
def swipe():
    frames = []
    for fi in range(4):
        c = C(48, 8)
        p = (fi + 1) / 4 * 48
        L = 30
        for x in range(48):
            u = x + 0.5
            if u > p or u < p - L:
                continue
            k = (u - (p - L)) / L
            t = 1 + 6 * k
            if fi == 3:
                t = min(t, 3)
                if int(u) % 6 < 2:
                    continue
            if u > p - 2:
                t = min(t, 2 + (p - u) * 2)
            for y in range(int(t)):
                col = "w4" if y == 0 else ("w3" if y < 3 else "w2")
                if y == int(t) - 1 and t > 2:
                    col = "w1"
                c.px[y][x] = col
            if x % 7 == 3 and int(t) < 8:
                c.px[min(7, int(t))][x] = "e1"
        frames.append(c)
    return stack(frames)


# ---------- 苏醒咆哮冲击环（面向镜头 4×4 格，5 帧）----------
def roar():
    frames = []
    for fi in range(5):
        c = C(32, 32)
        r = [5, 8, 11, 13.2, 14.6][fi]
        th = [3, 3, 2.2, 2, 1.2][fi]

        def f(px, py):
            dx, dy = px - 16, py - 16
            d = math.hypot(dx, dy)
            a = math.degrees(math.atan2(dy, dx)) % 360
            if abs(d - r) < th / 2:
                if fi >= 3 and int(a // 20) % 3 == 2:
                    return None
                return "a1" if d > r else "a2"
            if fi <= 2:
                for k in range(8):
                    if abs(adiff(a, k * 45 + 22.5)) * math.pi / 180 * d < 0.6 and r + 2 < d < r + 4.5:
                        return "a2"
            return None

        c.fill(f)
        c.outline()
        frames.append(c)
    return stack(frames)


# ---------- 阶段横幅 160×32 ----------
def banner():
    c = C(160, 32)
    for y in range(5, 27):
        for x in range(6, 154):
            edge = y in (5, 26) or x in (6, 153)
            c.px[y][x] = "b1" if edge else "w0"
    for x in range(10, 150):
        c.px[7][x] = "w1"
        c.px[24][x] = "w1"
    for x0 in list(range(18, 46, 6)) + list(range(116, 144, 6)):
        for i in range(3):
            w = 2 - i
            for x in range(x0 - w, x0 + w + 1):
                c.set(x, 6 + i, "b2")
                c.set(x, 25 - i, "b1")
    for cx in (8, 151):
        def f(px, py, cx=cx):
            d = math.hypot(px - cx - 0.5, py - 16)
            if d > 7.5:
                return None
            if d < 2:
                return "a1" if d < 0.9 else "O"
            if d > 6.6:
                return "b0"
            return "b2" if (px - cx) + (py - 16) < -3 else "b1"

        c.fill(f)
        for i in range(4):
            c.set(cx - 2 + i * 1.5, 21, "O")
    c.outline()
    return c.image()


# ---------- 物品：远古化石之饵 16×16（骨质鱼钩 + 绑在钩柄上的海晶碎片）----------
def bait():
    c = C(16, 16)
    pts = [(11.5, 2.2), (11.5, 10)]
    for i in range(1, 13):
        a = math.radians(i * 15)
        pts.append((7.6 + 3.9 * math.cos(a), 10 + 3.9 * math.sin(a)))
    pts.append((3.0, 6.6))
    segs = [(pts[i], pts[i + 1], 1.55 if i < len(pts) - 3 else 1.1) for i in range(len(pts) - 1)]

    def near(px, py):
        best = None
        for (x0, y0), (x1, y1), rad in segs:
            vx, vy = x1 - x0, y1 - y0
            t = max(0, min(1, ((px - x0) * vx + (py - y0) * vy) / (vx * vx + vy * vy)))
            qx, qy = x0 + vx * t, y0 + vy * t
            d = math.hypot(px - qx, py - qy)
            if d <= rad and (best is None or d < best[0]):
                best = (d, px - qx, py - qy, rad)
        return best

    def f(px, py):
        if math.hypot(px - 11.5, py - 1.6) < 1.6:
            return "b2" if px < 11.5 else "b1"
        n = near(px, py)
        if n is None:
            return None
        _, ox, oy, rad = n
        s = (-ox - oy) / max(rad, 0.01)
        return "b2" if s > 0.3 else ("b0" if s < -0.45 else "b1")

    c.fill(f)
    c.set(3, 5, "b2")
    c.set(4, 6, "b1")

    def shard(px, py):
        if abs(px - 7.6) / 2.1 + abs(py - 5.0) / 3.9 < 1:
            if py < 3.2:
                return "w4"
            return "w3" if px < 7.6 else ("w1" if py > 6.5 else "w2")
        return None

    c.fill(shard)
    for x in range(8, 14):
        c.set(x, 4, "m1")
        c.set(x, 5, "m0")
    c.set(13, 6, "m0")
    c.outline()
    c.set(14, 9, "w3")
    c.set(1, 2, "w3")
    return c.image()

def main():
    fx = os.path.join(TEX, "fx", "swampjaw")
    os.makedirs(fx, exist_ok=True)
    os.makedirs(os.path.join(TEX, "item"), exist_ok=True)
    out = {
        "fx/swampjaw/fang": fang(),
        "fx/swampjaw/fang_mark": fang_mark(),
        "fx/swampjaw/fin": fin(),
        "fx/swampjaw/fin_blade": fin_blade(),
        "fx/swampjaw/breach": breach(),
        "fx/swampjaw/maw": maw(),
        "fx/swampjaw/charge": charge(),
        "fx/swampjaw/bone_spike": bone_spike(),
        "fx/swampjaw/spout": spout(),
        "fx/swampjaw/swipe": swipe(),
        "fx/swampjaw/roar": roar(),
        "fx/banner/swampjaw": banner(),
        "item/ancient_fossil_bait": bait(),
    }
    for i, n in enumerate((32, 24, 16, 12)):
        out["fx/swampjaw/drop_mark_%d" % i] = drop_mark(n)
    for k, im in out.items():
        im.save(os.path.join(TEX, k + ".png"))
    if len(sys.argv) > 1:
        preview(out, sys.argv[1])


def preview(out, path, s=6):
    items = list(out.items())
    pad = 8
    W = sum(im.width * s + pad for _, im in items) + pad
    H = max(im.height * s for _, im in items) + pad * 2
    if W > 4000:
        half = len(items) // 2
        a, b = items[:half], items[half:]
        ia, ib = sheet(a, s, pad), sheet(b, s, pad)
        im = Image.new("RGBA", (max(ia.width, ib.width), ia.height + ib.height), (90, 90, 90, 255))
        im.paste(ia, (0, 0))
        im.paste(ib, (0, ia.height))
    else:
        im = sheet(items, s, pad)
    im.save(path)


def sheet(items, s, pad):
    W = sum(im.width * s + pad for _, im in items) + pad
    H = max(im.height * s for _, im in items) + pad * 2
    bg = Image.new("RGBA", (W, H), (0, 0, 0, 255))
    for y in range(0, H, 12):
        for x in range(0, W, 12):
            g = 120 if (x // 12 + y // 12) % 2 else 150
            bg.paste((g, g, g, 255), (x, y, x + 12, y + 12))
    x = pad
    for _, im in items:
        big = im.resize((im.width * s, im.height * s), Image.NEAREST)
        bg.alpha_composite(big, (x, pad))
        x += big.width + pad
    return bg


if __name__ == "__main__":
    main()
