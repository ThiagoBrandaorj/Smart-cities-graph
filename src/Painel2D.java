package src;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/** Quadro 2D: entrada visual do grafo (criar, ligar, mover, apagar) e saída com destaque dos algoritmos. */
public class Painel2D extends JPanel {
    static final int R = 22;
    static final Color VERDE = new Color(0x0F9D6B);
    final Modelo m;
    String modo = "vertice", sel, arrasto;

    Painel2D(Modelo m) {
        this.m = m;
        setBackground(new Color(0xFAFAF7)); setPreferredSize(new Dimension(760, 520));
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { clicou(e); }
            @Override public void mouseDragged(MouseEvent e) {
                if (arrasto != null) { m.pos.put(arrasto, new Point2D.Double(e.getX(), e.getY())); m.repintar(); }
            }
            @Override public void mouseReleased(MouseEvent e) { arrasto = null; }
        };
        addMouseListener(ma); addMouseMotionListener(ma);
        m.aoRepintar.add(this::repaint);
    }

    String vertice(double x, double y) {
        for (var e : m.pos.entrySet()) if (Math.hypot(e.getValue().x - x, e.getValue().y - y) <= R) return e.getKey();
        return null;
    }
    void clicou(MouseEvent e) {
        double x = e.getX(), y = e.getY(); String n = vertice(x, y);
        if (e.getClickCount() == 2 && n != null) { renomear(n); return; }
        switch (modo) {
            case "vertice" -> {
                if (n == null && e.getClickCount() == 1) {
                    String nome = m.proximoNome(); m.g.addVertice(nome); m.pos.put(nome, new Point2D.Double(x, y)); m.estruturaMudou();
                }
            }
            case "aresta" -> {
                if (n == null) sel = null;
                else if (sel == null) sel = n;
                else { boolean ok = !n.equals(sel) && novaAresta(sel, n); sel = null; if (ok) { m.estruturaMudou(); return; } }
                repaint();
            }
            case "mover" -> arrasto = n;
            case "apagar" -> {
                if (n != null) { m.g.removerVertice(n); m.pos.remove(n); m.estruturaMudou(); }
                else for (var a : m.g.arestas()) {
                    var p = pontas(a.u(), a.v());
                    if (Line2D.ptSegDist(p[0].x, p[0].y, p[1].x, p[1].y, x, y) < 8) { m.g.removerAresta(a.u(), a.v()); m.estruturaMudou(); break; }
                }
            }
        }
    }
    boolean novaAresta(String u, String v) {
        double w = 1;
        if (m.g.ponderado) {
            String s = JOptionPane.showInputDialog(this, "Peso da aresta " + u + " → " + v + ":", "1");
            if (s == null) return false;
            try { w = Double.parseDouble(s.trim().replace(',', '.')); } catch (NumberFormatException ex) { return false; }
        }
        m.g.addAresta(u, v, w); return true;
    }
    void renomear(String a) {
        String b = JOptionPane.showInputDialog(this, "Novo nome do vértice:", a);
        if (b == null || (b = b.trim()).isEmpty() || b.equals(a)) return;
        if (m.pos.containsKey(b)) { JOptionPane.showMessageDialog(this, "Já existe um vértice com esse nome."); return; }
        m.g.renomear(a, b); m.pos.put(b, m.pos.remove(a)); sel = null; m.estruturaMudou();
    }

    /** Pontos de início e fim da aresta, na borda dos círculos (com leve desvio se houver aresta de volta). */
    Point2D.Double[] pontas(String u, String v) {
        var a = m.pos.get(u); var b = m.pos.get(v);
        double dx = b.x - a.x, dy = b.y - a.y, L = Math.max(Math.hypot(dx, dy), 1e-6), ux = dx / L, uy = dy / L;
        double o = m.g.direcionado && m.g.temAresta(v, u) ? 7 : 0, px = -uy * o, py = ux * o;
        return new Point2D.Double[]{new Point2D.Double(a.x + ux * R + px, a.y + uy * R + py), new Point2D.Double(b.x - ux * R + px, b.y - uy * R + py)};
    }
    static void centralizar(Graphics2D g, String t, double x, double y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(t, (float) (x - fm.stringWidth(t) / 2.0), (float) (y + (fm.getAscent() - fm.getDescent()) / 2.0));
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for (var a : m.g.arestas()) {
            var p = pontas(a.u(), a.v()); boolean on = m.arestas.contains(m.g.chave(a.u(), a.v()));
            g.setColor(on ? VERDE : new Color(0x888888)); g.setStroke(new BasicStroke(on ? 4f : 1.8f));
            g.draw(new Line2D.Double(p[0], p[1]));
            if (m.g.direcionado) {
                double an = Math.atan2(p[1].y - p[0].y, p[1].x - p[0].x);
                Path2D.Double s = new Path2D.Double(); s.moveTo(p[1].x, p[1].y);
                s.lineTo(p[1].x - 14 * Math.cos(an - .4), p[1].y - 14 * Math.sin(an - .4));
                s.lineTo(p[1].x - 14 * Math.cos(an + .4), p[1].y - 14 * Math.sin(an + .4)); g.fill(s);
            }
            if (m.g.ponderado) {
                String t = Algoritmos.fmt(a.peso()); double mx = (p[0].x + p[1].x) / 2, my = (p[0].y + p[1].y) / 2;
                g.setFont(new Font("SansSerif", Font.BOLD, 14)); int w = g.getFontMetrics().stringWidth(t) + 8;
                g.setColor(getBackground()); g.fill(new Rectangle2D.Double(mx - w / 2.0, my - 10, w, 20));
                g.setColor(on ? new Color(0x0A6B49) : new Color(0x333333)); centralizar(g, t, mx, my);
            }
        }
        g.setStroke(new BasicStroke(2f));
        for (var e : m.pos.entrySet()) {
            String n = e.getKey(), l = m.nos.get(n); boolean on = l != null; var p = e.getValue();
            Ellipse2D c = new Ellipse2D.Double(p.x - R, p.y - R, 2 * R, 2 * R);
            g.setColor(n.equals(sel) ? new Color(0xFFE08A) : on ? new Color(0xBFE9D8) : Color.WHITE); g.fill(c);
            g.setColor(on ? VERDE : new Color(0x333333)); g.draw(c);
            g.setColor(Color.BLACK); g.setFont(new Font("SansSerif", Font.BOLD, 16)); centralizar(g, n, p.x, p.y);
            if (on && !l.isEmpty()) { g.setColor(new Color(0x0A6B49)); g.setFont(new Font("SansSerif", Font.BOLD, 13)); centralizar(g, l, p.x, p.y - R - 9); }
        }
    }
}
