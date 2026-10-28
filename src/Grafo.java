package src;
import java.util.*;

/** Grafo em lista de adjacência (vértice -> vizinhos), sem uso de bibliotecas de grafos. */
public class Grafo {
    public static class Vizinho {
        String para; double peso;
        Vizinho(String para, double peso) { this.para = para; this.peso = peso; }
    }
    public record Aresta(String u, String v, double peso) {}

    boolean direcionado, ponderado;
    LinkedHashMap<String, List<Vizinho>> lista = new LinkedHashMap<>();

    Grafo(boolean direcionado, boolean ponderado) { this.direcionado = direcionado; this.ponderado = ponderado; }

    /** Peso efetivo: 1 quando o grafo não é ponderado. */
    double w(double peso) { return ponderado ? peso : 1; }

    String chave(String u, String v) {
        return direcionado || u.compareTo(v) <= 0 ? u + ">" + v : v + ">" + u;
    }
    void addVertice(String n) { lista.putIfAbsent(n, new ArrayList<>()); }

    boolean temAresta(String u, String v) {
        return lista.containsKey(u) && lista.get(u).stream().anyMatch(x -> x.para.equals(v));
    }
    private void definir(String u, String v, double w) {
        for (Vizinho x : lista.get(u)) if (x.para.equals(v)) { x.peso = w; return; }
        lista.get(u).add(new Vizinho(v, w));
    }
    void addAresta(String u, String v, double w) {
        if (u.equals(v)) return;
        addVertice(u); addVertice(v);
        definir(u, v, w);
        if (!direcionado) definir(v, u, w);
    }
    void removerAresta(String u, String v) {
        lista.get(u).removeIf(x -> x.para.equals(v));
        if (!direcionado) lista.get(v).removeIf(x -> x.para.equals(u));
    }
    void removerVertice(String n) {
        lista.remove(n);
        lista.values().forEach(l -> l.removeIf(x -> x.para.equals(n)));
    }
    void renomear(String a, String b) {
        LinkedHashMap<String, List<Vizinho>> nova = new LinkedHashMap<>();
        for (var e : lista.entrySet()) {
            for (Vizinho x : e.getValue()) if (x.para.equals(a)) x.para = b;
            nova.put(e.getKey().equals(a) ? b : e.getKey(), e.getValue());
        }
        lista = nova;
    }
    /** Lista de arestas; em grafos não-direcionados cada aresta aparece uma vez. */
    List<Aresta> arestas() {
        List<Aresta> r = new ArrayList<>(); Set<String> vistas = new HashSet<>();
        for (var e : lista.entrySet())
            for (Vizinho x : e.getValue()) {
                if (!direcionado && !vistas.add(chave(e.getKey(), x.para))) continue;
                r.add(new Aresta(e.getKey(), x.para, x.peso));
            }
        return r;
    }
}
