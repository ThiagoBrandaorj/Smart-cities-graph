package src;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.Timer;

/** Estado compartilhado entre a visão 2D, a visão 3D e os painéis laterais. */
public class Modelo {
    Grafo g = new Grafo(false, true);
    final Map<String, Point2D.Double> pos = new LinkedHashMap<>();
    final Map<String, String> nos = new HashMap<>();   // vértice destacado -> rótulo
    final Set<String> arestas = new HashSet<>();        // chaves das arestas destacadas
    String origem;
    final List<Runnable> aoMudarEstrutura = new ArrayList<>(), aoRepintar = new ArrayList<>();
    private Timer anim;

    void limparDestaque() { if (anim != null) anim.stop(); nos.clear(); arestas.clear(); }
    void estruturaMudou() { limparDestaque(); aoMudarEstrutura.forEach(Runnable::run); repintar(); }
    void repintar() { aoRepintar.forEach(Runnable::run); }

    String proximoNome() {
        for (char c = 'A'; c <= 'Z'; c++) if (!pos.containsKey(String.valueOf(c))) return String.valueOf(c);
        int i = 27; while (pos.containsKey("V" + i)) i++;
        return "V" + i;
    }
    /** Revela os passos do algoritmo, um a cada 450 ms. */
    void animar(List<Algoritmos.Passo> passos) {
        limparDestaque();
        int[] i = {0};
        anim = new Timer(450, e -> {
            if (i[0] >= passos.size()) { anim.stop(); return; }
            var p = passos.get(i[0]++);
            if (p.no() != null) nos.put(p.no(), p.rotulo());
            if (p.u() != null) {
                arestas.add(g.chave(p.u(), p.v())); nos.putIfAbsent(p.u(), ""); nos.putIfAbsent(p.v(), "");
            }
            repintar();
        });
        anim.start();
    }
    /** Grafo de teste do enunciado. */
    void carregarExemplo(double larg, double alt) {
        g = new Grafo(false, true); pos.clear();
        for (String t : "O .08 .45;A .30 .13;F .80 .13;B .36 .50;D .62 .50;T .90 .52;C .20 .88;E .52 .88".split(";")) {
            String[] c = t.split(" ");
            g.addVertice(c[0]); pos.put(c[0], new Point2D.Double(Double.parseDouble(c[1]) * larg, Double.parseDouble(c[2]) * alt));
        }
        for (String t : "O A 2,O B 5,O C 4,A B 2,A D 7,A F 12,B C 1,B D 4,B E 3,C E 4,D E 4,D T 5,E T 7,F T 3".split(",")) {
            String[] c = t.split(" ");
            g.addAresta(c[0], c[1], Double.parseDouble(c[2]));
        }
    }
}
