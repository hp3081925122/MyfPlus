package com.hp.myfplus.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;

public record TintedConsumer(VertexConsumer inner, float r, float g, float b, float a) implements VertexConsumer {
    @Override
    public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float nx, float ny, float nz) {
        inner.vertex(x, y, z, r, g, b, a, u, v, overlay, light, nx, ny, nz);
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        inner.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        inner.color((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        inner.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        inner.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        inner.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        inner.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        inner.endVertex();
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        inner.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        inner.unsetDefaultColor();
    }
}
