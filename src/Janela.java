package src;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/** Janela principal do SmartNet Analyzer. */
public class Janela extends JFrame {
    final Modelo m = new Modelo();
    final Painel2D p2 = new Painel2D(m);
    final JComboBox<String> origem = new JComboBox<>();
    final Painel3D p3 = new Painel3D(m, origem::setSelectedItem);
    final CardLayout cartas = new CardLayout();
    final JPanel centro = new JPanel(cartas), modos = new JPanel();
    final JEditorPane res = html(), estr = html();
    final JLabel dica = new JLabel(" ");
    final JCheckBox dir = new JCheckBox("Direcionado"), pond = new JCheckBox("Ponderado", true);
    static final Map<String, String> DICAS = Map.of(
        "vertice", "Clique em um espaço vazio para criar um vértice. Duplo clique em um vértice para renomear.",
        "aresta", "Clique no vértice de origem e depois no de destino para criar a aresta.",
        "mover", "Arraste os vértices para reorganizar o desenho.",
        "apagar", "Clique em um vértice ou em uma aresta para apagá-lo.",
        "3d", "3D: visão sincronizada com o grafo. Para criar ou editar vértices e arestas, volte ao 2D.");

    Janela() {
        super("SmartNet Analyzer");
        setDefaultCloseOperation(EXIT_ON_CLOSE); setLayout(new BorderLayout());

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        ButtonGroup gv = new ButtonGroup(), gm = new ButtonGroup();
        JRadioButton r2 = new JRadioButton("2D", true), r3 = new JRadioButton("3D");
        gv.add(r2); gv.add(r3); topo.add(r2); topo.add(r3);
        modos.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (String[] md : new String[][]{{"Vértice", "vertice"}, {"Aresta", "aresta"}, {"Mover", "mover"}, {"Apagar", "apagar"}}) {
            JRadioButton b = new JRadioButton(md[0], md[1].equals("vertice")); gm.add(b); modos.add(b);
            b.addActionListener(e -> { p2.modo = md[1]; p2.sel = null; dica.setText(DICAS.get(md[1])); p2.repaint(); });
        }
        topo.add(modos); topo.add(dir); topo.add(pond);
        JButton exemplo = new JButton("Carregar grafo do enunciado"), apagar = new JButton("Apagar grafo"), fim = new JButton("Finalizar aplicação");
        topo.add(exemplo); topo.add(apagar); topo.add(fim);
        add(topo, BorderLayout.NORTH);

        JPanel c3 = new JPanel(new BorderLayout()), barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox auto = new JCheckBox("Girar automaticamente", true); JButton rec = new JButton("Recentrar câmera");
        auto.addActionListener(e -> p3.auto = auto.isSelected()); rec.addActionListener(e -> p3.recentrar());
        barra.add(auto); barra.add(rec); c3.add(p3, BorderLayout.CENTER); c3.add(barra, BorderLayout.SOUTH);
        centro.add(p2, "2d"); centro.add(c3, "3d");
        JPanel esq = new JPanel(new BorderLayout()); esq.add(centro, BorderLayout.CENTER); esq.add(dica, BorderLayout.SOUTH);
        add(esq, BorderLayout.CENTER);
        dica.setText(DICAS.get("vertice"));
        r2.addActionListener(e -> { cartas.show(centro, "2d"); modos.setVisible(true); dica.setText(DICAS.get(p2.modo)); });
        r3.addActionListener(e -> { cartas.show(centro, "3d"); modos.setVisible(false); dica.setText(DICAS.get("3d")); });

        JPanel alg = new JPanel(); alg.setLayout(new BoxLayout(alg, BoxLayout.Y_AXIS)); alg.setBorder(new TitledBorder("Algoritmos"));
        JPanel l1 = new JPanel(new FlowLayout(FlowLayout.LEFT)), l2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        l1.add(new JLabel("Origem:")); l1.add(origem);
        for (String a : new String[]{"DFS", "BFS", "Dijkstra", "Kruskal"}) { JButton b = new JButton(a); b.addActionListener(e -> executar(a)); l2.add(b); }
        alg.add(l1); alg.add(l2);
        JScrollPane sr = new JScrollPane(res), se = new JScrollPane(estr);
        sr.setBorder(new TitledBorder("Resultado")); se.setBorder(new TitledBorder("Memória: lista de adjacência"));
        JSplitPane div = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sr, se); div.setResizeWeight(.6);
        JPanel lado = new JPanel(new BorderLayout()); lado.add(alg, BorderLayout.NORTH); lado.add(div, BorderLayout.CENTER);
        lado.setPreferredSize(new Dimension(430, 0)); add(lado, BorderLayout.EAST);

        m.aoMudarEstrutura.add(this::atualizarEstrutura);
        origem.addActionListener(e -> { m.origem = (String) origem.getSelectedItem(); m.repintar(); });
        dir.addActionListener(e -> {
            Grafo n = new Grafo(dir.isSelected(), m.g.ponderado);
            m.g.lista.keySet().forEach(n::addVertice); m.g.arestas().forEach(a -> n.addAresta(a.u(), a.v(), a.peso()));
            m.g = n; m.estruturaMudou();
        });
        pond.addActionListener(e -> { m.g.ponderado = pond.isSelected(); m.estruturaMudou(); });
        apagar.addActionListener(e -> { m.g = new Grafo(dir.isSelected(), pond.isSelected()); m.pos.clear(); res.setText(""); m.estruturaMudou(); });
        exemplo.addActionListener(e -> {
            dir.setSelected(false); pond.setSelected(true);
            m.carregarExemplo(p2.getWidth() > 0 ? p2.getWidth() : 760, p2.getHeight() > 0 ? p2.getHeight() : 520);
            res.setText(""); m.estruturaMudou();
        });
        fim.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Finalizar a aplicação?", "SmartNet Analyzer", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { dispose(); System.exit(0); }
        });
        atualizarEstrutura();
        setSize(1260, 780); setLocationRelativeTo(null);
    }

    static JEditorPane html() { JEditorPane e = new JEditorPane("text/html", ""); e.setEditable(false); return e; }

    void atualizarEstrutura() {
        Object ant = origem.getSelectedItem(); origem.removeAllItems();
        for (String v : m.g.lista.keySet()) origem.addItem(v);
        if (ant != null && m.g.lista.containsKey(ant)) origem.setSelectedItem(ant);
        StringBuilder sb = new StringBuilder("<html><body style='font-family:SansSerif'><table border='1' cellpadding='3'>");
        for (var e : m.g.lista.entrySet()) {
            List<String> viz = new ArrayList<>();
            for (var x : e.getValue()) viz.add("[" + x.para + (m.g.ponderado ? "|" + Algoritmos.fmt(x.peso) : "") + "]");
            sb.append("<tr><td><b>").append(e.getKey()).append("</b></td><td>").append(viz.isEmpty() ? "∅" : String.join(" → ", viz)).append("</td></tr>");
        }
        estr.setText(m.g.lista.isEmpty() ? "<html><body><i>Grafo vazio.</i></body></html>" : sb.append("</table></body></html>").toString());
    }
    void mostrar(String titulo, String corpo) {
        res.setText("<html><body style='font-family:SansSerif'><h3>" + titulo + "</h3>" + corpo + "</body></html>"); res.setCaretPosition(0);
    }
    static String tabela(String[] cab, List<String[]> linhas) {
        StringBuilder sb = new StringBuilder("<table border='1' cellpadding='3'><tr>");
        for (String c : cab) sb.append("<th>").append(c).append("</th>");
        sb.append("</tr>");
        for (String[] l : linhas) { sb.append("<tr>"); for (String c : l) sb.append("<td>").append(c).append("</td>"); sb.append("</tr>"); }
        return sb.append("</table>").toString();
    }

    void executar(String alg) {
        if (m.pos.isEmpty()) { mostrar("Aviso", "<p>Desenhe um grafo primeiro.</p>"); return; }
        String s = (String) origem.getSelectedItem();
        try {
            switch (alg) {
                case "DFS" -> {
                    var r = Algoritmos.dfs(m.g, s);
                    mostrar("DFS a partir de " + s, "<p><b>Ordem de visita:</b> " + String.join(" → ", r.ordem()) + "</p><pre>" + String.join("\n", r.linhas())
                        + "</pre><p><b>Conectividade:</b> " + (r.naoAlcancados().isEmpty() ? "todos os vértices são alcançáveis a partir de " + s : String.join(", ", r.naoAlcancados()) + " não alcançado(s) a partir de " + s)
                        + ".</p><p><b>Becos sem saída:</b> " + (r.becos().isEmpty() ? "nenhum" : String.join(", ", r.becos())) + "</p><p><i>Complexidade: O(V+E)</i></p>");
                    m.animar(r.passos());
                }
                case "BFS" -> {
                    var r = Algoritmos.bfs(m.g, s); List<String[]> l = new ArrayList<>();
                    r.saltos().forEach((v, d) -> l.add(new String[]{v, String.valueOf(d)}));
                    mostrar("BFS a partir de " + s, "<p><b>Fila de execução:</b> " + String.join(", ", r.fila()) + "</p>" + tabela(new String[]{"Vértice", "Saltos"}, l)
                        + "<p>" + (r.naoAlcancados().isEmpty() ? "Todos alcançados." : "Não alcançados: " + String.join(", ", r.naoAlcancados())) + "</p><p><i>Menor caminho em nº de arestas. Complexidade: O(V+E)</i></p>");
                    m.animar(r.passos());
                }
                case "Dijkstra" -> {
                    var r = Algoritmos.dijkstra(m.g, s); List<String[]> l = new ArrayList<>();
                    r.dist().forEach((v, d) -> l.add(new String[]{v, d.isInfinite() ? "∞" : Algoritmos.fmt(d), d.isInfinite() ? "inalcançável" : String.join(" → ", r.caminho(v))}));
                    mostrar("Dijkstra a partir de " + s, tabela(new String[]{"Vértice", "Distância", "Caminho"}, l) + "<p><i>Complexidade: O((V+E) log V), heap binário</i></p>");
                    m.animar(r.passos());
                }
                default -> {
                    var r = Algoritmos.kruskal(m.g); List<String[]> l = new ArrayList<>(); List<String> esc = new ArrayList<>();
                    for (var x : r.log()) l.add(new String[]{x.aresta().u() + "–" + x.aresta().v(), Algoritmos.fmt(x.custo()), x.aceita() ? "✔ aceita" : "✘ forma ciclo"});
                    for (var a : r.escolhidas()) esc.add(a.u() + "–" + a.v());
                    mostrar("Kruskal (árvore geradora mínima)", (m.g.direcionado ? "<p><i>Grafo direcionado: arestas tratadas como não-direcionadas.</i></p>" : "")
                        + tabela(new String[]{"Aresta", "Peso", "Decisão"}, l) + "<p><b>Arestas escolhidas:</b> " + (esc.isEmpty() ? "nenhuma" : String.join(", ", esc))
                        + "</p><p><b>Custo total: " + Algoritmos.fmt(r.total()) + "</b>" + (r.geradora() ? "" : " (grafo desconexo: floresta geradora mínima)")
                        + "</p><p><i>Complexidade: O(E log E), Union-Find</i></p>");
                    m.animar(r.passos());
                }
            }
        } catch (IllegalArgumentException ex) { mostrar("Erro", "<p style='color:red'><b>" + ex.getMessage() + "</b></p>"); }
    }
}
