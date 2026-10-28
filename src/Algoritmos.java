package src;
import java.util.*;

import src.Grafo.Aresta;
import src.Grafo.Vizinho;

/** DFS, BFS, Dijkstra e Kruskal. Cada algoritmo devolve também os "passos" usados na animação. */
public class Algoritmos {
    public record Passo(String no, String rotulo, String u, String v) {
        static Passo visita(String n, String rot) { return new Passo(n, rot, null, null); }
        static Passo via(String u, String v) { return new Passo(null, null, u, v); }
    }
    public record ResDfs(List<String> ordem, List<Passo> passos, List<String> linhas, List<String> naoAlcancados, List<String> becos) {}
    public record ResBfs(List<String> fila, Map<String, Integer> saltos, List<Passo> passos, List<String> naoAlcancados) {}
    public record ResDij(Map<String, Double> dist, Map<String, String> ant, List<Passo> passos) {
        List<String> caminho(String v) {
            if (dist.get(v).isInfinite()) return null;
            LinkedList<String> p = new LinkedList<>();
            for (String c = v; c != null; c = ant.get(c)) p.addFirst(c);
            return p;
        }
    }
    public record Linha(Grafo.Aresta aresta, double custo, boolean aceita) {}
    public record ResKru(List<Linha> log, List<Grafo.Aresta> escolhidas, double total, boolean geradora, List<Passo> passos) {}

    static String fmt(double d) { return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d); }

    // ---- DFS recursiva: O(V+E)
    public static ResDfs dfs(Grafo g, String s) {
        List<String> ordem = new ArrayList<>(), linhas = new ArrayList<>(); List<Passo> passos = new ArrayList<>();
        dfs(g, s, null, 0, new HashSet<>(), ordem, passos, linhas);
        List<String> fora = new ArrayList<>(), becos = new ArrayList<>();
        for (var e : g.lista.entrySet()) {
            if (!ordem.contains(e.getKey())) fora.add(e.getKey());
            if (e.getValue().size() <= (g.direcionado ? 0 : 1)) becos.add(e.getKey());
        }
        return new ResDfs(ordem, passos, linhas, fora, becos);
    }
    private static void dfs(Grafo g, String u, String pai, int prof, Set<String> vis, List<String> ordem, List<Passo> passos, List<String> linhas) {
        vis.add(u); ordem.add(u);
        if (pai != null) passos.add(Passo.via(pai, u));
        passos.add(Passo.visita(u, String.valueOf(ordem.size())));
        linhas.add("    ".repeat(prof) + (pai != null ? "└─ " : "") + u);
        for (var x : g.lista.get(u)) if (!vis.contains(x.para)) dfs(g, x.para, u, prof + 1, vis, ordem, passos, linhas);
    }

    // ---- BFS com fila em vetor (índice de leitura): O(V+E)
    public static ResBfs bfs(Grafo g, String s) {
        Map<String, Integer> saltos = new LinkedHashMap<>(); saltos.put(s, 0);
        List<String> fila = new ArrayList<>(); fila.add(s);
        List<Passo> passos = new ArrayList<>(); passos.add(Passo.visita(s, "0"));
        for (int i = 0; i < fila.size(); i++) {
            String u = fila.get(i);
            for (var x : g.lista.get(u)) if (!saltos.containsKey(x.para)) {
                saltos.put(x.para, saltos.get(u) + 1); fila.add(x.para);
                passos.add(Passo.via(u, x.para)); passos.add(Passo.visita(x.para, String.valueOf(saltos.get(x.para))));
            }
        }
        List<String> fora = new ArrayList<>();
        for (String v : g.lista.keySet()) if (!saltos.containsKey(v)) fora.add(v);
        return new ResBfs(fila, saltos, passos, fora);
    }

    // ---- Dijkstra com fila de prioridade (heap binário): O((V+E) log V)
    public static ResDij dijkstra(Grafo g, String s) {
        for (var a : g.arestas())
            if (g.w(a.peso()) < 0) throw new IllegalArgumentException("Dijkstra não aceita pesos negativos (aresta " + a.u() + "–" + a.v() + ").");
        Map<String, Double> dist = new LinkedHashMap<>();
        for (String v : g.lista.keySet()) dist.put(v, Double.POSITIVE_INFINITY);
        Map<String, String> ant = new HashMap<>(); Set<String> fechado = new HashSet<>(); List<Passo> passos = new ArrayList<>();
        PriorityQueue<Map.Entry<String, Double>> fila = new PriorityQueue<>(Map.Entry.comparingByValue());
        dist.put(s, 0.0); fila.add(Map.entry(s, 0.0));
        while (!fila.isEmpty()) {
            var e = fila.poll(); String u = e.getKey(); double d = e.getValue();
            if (!fechado.add(u)) continue;
            if (ant.containsKey(u)) passos.add(Passo.via(ant.get(u), u));
            passos.add(Passo.visita(u, fmt(d)));
            for (var x : g.lista.get(u)) {
                double nd = d + g.w(x.peso);
                if (nd < dist.get(x.para)) { dist.put(x.para, nd); ant.put(x.para, u); fila.add(Map.entry(x.para, nd)); }
            }
        }
        return new ResDij(dist, ant, passos);
    }

    // ---- Kruskal com Union-Find (rank + compressão de caminho): O(E log E)
    static class UF {
        final Map<String, String> pai = new HashMap<>(); final Map<String, Integer> rank = new HashMap<>();
        UF(Collection<String> vs) { for (String v : vs) { pai.put(v, v); rank.put(v, 0); } }
        String achar(String x) {
            while (!pai.get(x).equals(x)) { pai.put(x, pai.get(pai.get(x))); x = pai.get(x); }
            return x;
        }
        boolean unir(String a, String b) {
            a = achar(a); b = achar(b);
            if (a.equals(b)) return false;
            if (rank.get(a) < rank.get(b)) { String t = a; a = b; b = t; }
            pai.put(b, a);
            if (rank.get(a).equals(rank.get(b))) rank.merge(a, 1, Integer::sum);
            return true;
        }
    }
    public static ResKru kruskal(Grafo g) {
        List<Grafo.Aresta> ord = new ArrayList<>(g.arestas());
        ord.sort(Comparator.comparingDouble((Grafo.Aresta a) -> g.w(a.peso())).thenComparing(Grafo.Aresta::u).thenComparing(Grafo.Aresta::v));
        UF uf = new UF(g.lista.keySet());
        List<Linha> log = new ArrayList<>(); List<Grafo.Aresta> esc = new ArrayList<>(); List<Passo> passos = new ArrayList<>();
        double total = 0;
        for (Grafo.Aresta a : ord) {
            boolean ok = uf.unir(a.u(), a.v());
            log.add(new Linha(a, g.w(a.peso()), ok));
            if (ok) { esc.add(a); total += g.w(a.peso()); passos.add(Passo.via(a.u(), a.v())); }
        }
        return new ResKru(log, esc, total, esc.size() == g.lista.size() - 1, passos);
    }
}
