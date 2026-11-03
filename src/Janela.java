package src;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

// Janela principal
public class Janela extends JFrame {

    Modelo m = new Modelo();
    Painel2D painel2D = new Painel2D(m);
    Painel3D painel3D = new Painel3D(m);

    JComboBox<String> origem = new JComboBox<>();
    JCheckBox direcionado = new JCheckBox("Direcionado");
    JCheckBox ponderado = new JCheckBox("Ponderado", true);
    JEditorPane resultado = new JEditorPane("text/html", "");
    JEditorPane memoria = new JEditorPane("text/html", "");
    JLabel dica = new JLabel(" ");

    public Janela() {
        super("SmartNet Analyzer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        resultado.setEditable(false);
        memoria.setEditable(false);

        add(montarBarraDeCima(), BorderLayout.NORTH);
        add(montarCentro(), BorderLayout.CENTER);
        add(montarLadoDireito(), BorderLayout.EAST);

        m.aoRepintar = () -> {
            painel2D.repaint();
            painel3D.repaint();
        };
        m.aoMudarGrafo = () -> {
            atualizarListaDeOrigem();
            mostrarMemoria();
            painel3D.calcularPosicoes();
        };

        mostrarMemoria();
        setSize(1260, 780);
        setLocationRelativeTo(null);
    }

    JPanel montarBarraDeCima() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

        // 2D ou 3D
        JRadioButton botao2D = new JRadioButton("2D", true);
        JRadioButton botao3D = new JRadioButton("3D");
        ButtonGroup grupoVisao = new ButtonGroup();
        grupoVisao.add(botao2D);
        grupoVisao.add(botao3D);
        barra.add(botao2D);
        barra.add(botao3D);

        // modos de edicao do 2D
        JPanel modos = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ButtonGroup grupoModo = new ButtonGroup();
        String[][] listaModos = {
            {"Vértice", "vertice", "Clique em um espaço vazio para criar um vértice. Duplo clique em um vértice para renomear."},
            {"Aresta", "aresta", "Clique no vértice de origem e depois no de destino."},
            {"Mover", "mover", "Arraste os vértices para organizar o desenho."},
            {"Apagar", "apagar", "Clique em um vértice ou aresta para apagar."}
        };
        for (String[] item : listaModos) {
            JRadioButton b = new JRadioButton(item[0], item[1].equals("vertice"));
            b.addActionListener(e -> {
                painel2D.modo = item[1];
                painel2D.selecionado = -1;
                dica.setText(item[2]);
                painel2D.repaint();
            });
            grupoModo.add(b);
            modos.add(b);
        }
        dica.setText(listaModos[0][2]);
        barra.add(modos);

        botao2D.addActionListener(e -> {
            ((CardLayout) painelCentral.getLayout()).show(painelCentral, "2d");
            modos.setVisible(true);
        });
        botao3D.addActionListener(e -> {
            ((CardLayout) painelCentral.getLayout()).show(painelCentral, "3d");
            modos.setVisible(false);
            dica.setText("Visão 3D: para editar volte ao 2D.");
        });

        // tipo do grafo
        direcionado.addActionListener(e -> {
            m.g = m.g.copiar(direcionado.isSelected());
            m.grafoMudou();
        });
        ponderado.addActionListener(e -> {
            m.g.ponderado = ponderado.isSelected();
            m.grafoMudou();
        });
        barra.add(direcionado);
        barra.add(ponderado);

        JButton porTexto = new JButton("Inserir por texto");
        JButton exemplo = new JButton("Carregar grafo do enunciado");
        JButton apagar = new JButton("Apagar grafo");
        JButton finalizar = new JButton("Finalizar aplicação");
        finalizar.setBackground(new Color(192, 57, 43));
        finalizar.setForeground(Color.WHITE);

        porTexto.addActionListener(e -> inserirPorTexto());
        exemplo.addActionListener(e -> {
            direcionado.setSelected(false);
            ponderado.setSelected(true);
            m.carregarExemplo(larguraQuadro(), alturaQuadro());
            resultado.setText("");
            m.grafoMudou();
        });
        apagar.addActionListener(e -> {
            m.g = new Grafo(direcionado.isSelected(), ponderado.isSelected());
            resultado.setText("");
            m.grafoMudou();
        });
        finalizar.addActionListener(e -> {
            int resposta = JOptionPane.showConfirmDialog(this, "Finalizar a aplicação?",
                    "SmartNet Analyzer", JOptionPane.YES_NO_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        barra.add(porTexto);
        barra.add(exemplo);
        barra.add(apagar);
        barra.add(finalizar);
        return barra;
    }

    JPanel painelCentral = new JPanel(new CardLayout());

    JPanel montarCentro() {
        JPanel tela3D = new JPanel(new BorderLayout());
        JCheckBox girar = new JCheckBox("Girar automaticamente", true);
        girar.addActionListener(e -> painel3D.girarSozinho = girar.isSelected());
        tela3D.add(painel3D, BorderLayout.CENTER);
        tela3D.add(girar, BorderLayout.SOUTH);

        painelCentral.add(painel2D, "2d");
        painelCentral.add(tela3D, "3d");

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(painelCentral, BorderLayout.CENTER);
        centro.add(dica, BorderLayout.SOUTH);
        return centro;
    }

    JPanel montarLadoDireito() {
        JPanel algoritmos = new JPanel();
        algoritmos.setLayout(new BoxLayout(algoritmos, BoxLayout.Y_AXIS));
        algoritmos.setBorder(new TitledBorder("Algoritmos"));

        JPanel linhaOrigem = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaOrigem.add(new JLabel("Origem:"));
        linhaOrigem.add(origem);

        JPanel linhaBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] nomes = {"DFS", "BFS", "Dijkstra", "Kruskal"};
        for (String nome : nomes) {
            JButton b = new JButton(nome);
            b.addActionListener(e -> executar(nome));
            linhaBotoes.add(b);
        }
        algoritmos.add(linhaOrigem);
        algoritmos.add(linhaBotoes);

        JScrollPane rolagemResultado = new JScrollPane(resultado);
        rolagemResultado.setBorder(new TitledBorder("Resultado"));
        JScrollPane rolagemMemoria = new JScrollPane(memoria);
        rolagemMemoria.setBorder(new TitledBorder("Memória: lista de adjacência"));
        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rolagemResultado, rolagemMemoria);
        divisao.setResizeWeight(0.6);

        JPanel lado = new JPanel(new BorderLayout());
        lado.add(algoritmos, BorderLayout.NORTH);
        lado.add(divisao, BorderLayout.CENTER);
        lado.setPreferredSize(new Dimension(430, 0));
        return lado;
    }

    double larguraQuadro() {
        return painel2D.getWidth() > 0 ? painel2D.getWidth() : 760;
    }

    double alturaQuadro() {
        return painel2D.getHeight() > 0 ? painel2D.getHeight() : 520;
    }

    void atualizarListaDeOrigem() {
        Object antes = origem.getSelectedItem();
        origem.removeAllItems();
        for (String nome : m.g.nomes) origem.addItem(nome);
        if (antes != null && m.g.indice((String) antes) != -1) origem.setSelectedItem(antes);
    }

    // lista de adjacencia na tela
    void mostrarMemoria() {
        if (m.g.numVertices() == 0) {
            memoria.setText("<html><body><i>Grafo vazio.</i></body></html>");
            return;
        }
        StringBuilder html = new StringBuilder("<html><body style='font-family:SansSerif'><table border='1' cellpadding='3'>");
        for (int u = 0; u < m.g.numVertices(); u++) {
            html.append("<tr><td><b>").append(u).append(": ").append(m.g.nomes.get(u)).append("</b></td><td>");
            ArrayList<Grafo.Aresta> lista = m.g.adj.get(u);
            if (lista.isEmpty()) html.append("(vazia)");
            for (int i = 0; i < lista.size(); i++) {
                Grafo.Aresta a = lista.get(i);
                if (i > 0) html.append(" -&gt; ");
                html.append("[").append(m.g.nomes.get(a.destino));
                if (m.g.ponderado) html.append(" | ").append(Algoritmos.formatar(a.peso));
                html.append("]");
            }
            html.append("</td></tr>");
        }
        html.append("</table></body></html>");
        memoria.setText(html.toString());
    }

    void mostrarResultado(String titulo, String corpo) {
        resultado.setText("<html><body style='font-family:SansSerif'><h3>" + titulo + "</h3>" + corpo + "</body></html>");
        resultado.setCaretPosition(0);
    }

    // tabela html
    static String tabela(String[] cabecalho, ArrayList<String[]> linhas) {
        StringBuilder html = new StringBuilder("<table border='1' cellpadding='3'><tr>");
        for (String c : cabecalho) html.append("<th>").append(c).append("</th>");
        html.append("</tr>");
        for (String[] linha : linhas) {
            html.append("<tr>");
            for (String c : linha) html.append("<td>").append(c).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    // nomes separados
    String nomesDe(ArrayList<Integer> vertices, String separador) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertices.size(); i++) {
            if (i > 0) sb.append(separador);
            sb.append(m.g.nomes.get(vertices.get(i)));
        }
        return sb.toString();
    }

    void inserirPorTexto() {
        JTextArea area = new JTextArea(m.paraTexto(), 16, 28);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JCheckBox dir = new JCheckBox("Direcionado", direcionado.isSelected());
        JCheckBox pond = new JCheckBox("Ponderado", ponderado.isSelected());

        JPanel opcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        opcoes.add(dir);
        opcoes.add(pond);
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.add(new JLabel("<html>Uma aresta por linha: <b>origem destino peso</b> (ex.: A B 5).<br>"
                + "Sem peso vale 1. Um nome sozinho cria um vértice isolado.</html>"), BorderLayout.NORTH);
        painel.add(new JScrollPane(area), BorderLayout.CENTER);
        painel.add(opcoes, BorderLayout.SOUTH);

        // repete se tiver erro
        while (true) {
            int resposta = JOptionPane.showConfirmDialog(this, painel, "Inserir grafo por texto",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (resposta != JOptionPane.OK_OPTION) return;
            try {
                m.carregarTexto(area.getText(), dir.isSelected(), pond.isSelected(), larguraQuadro(), alturaQuadro());
                direcionado.setSelected(dir.isSelected());
                ponderado.setSelected(pond.isSelected());
                resultado.setText("");
                m.grafoMudou();
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro no texto", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void executar(String algoritmo) {
        if (m.g.numVertices() == 0 || origem.getSelectedItem() == null) {
            mostrarResultado("Aviso", "<p>Insira um grafo primeiro.</p>");
            return;
        }
        int s = m.g.indice((String) origem.getSelectedItem());

        if (algoritmo.equals("DFS")) {
            mostrarDfs(s);
        } else if (algoritmo.equals("BFS")) {
            mostrarBfs(s);
        } else if (algoritmo.equals("Dijkstra")) {
            mostrarDijkstra(s);
        } else {
            mostrarKruskal();
        }
    }

    void mostrarDfs(int s) {
        Algoritmos.Busca r = Algoritmos.dfs(m.g, s);
        int n = m.g.numVertices();

        StringBuilder ordem = new StringBuilder();
        StringBuilder arvore = new StringBuilder();
        boolean[] visitado = new boolean[n];
        for (int i = 0; i < r.quantos; i++) {
            int v = r.ordem[i];
            visitado[v] = true;
            if (i > 0) ordem.append(" -&gt; ");
            ordem.append(m.g.nomes.get(v));
            // arvore em texto
            int nivel = Math.min(r.nivel[v], 20);
            for (int k = 0; k < nivel; k++) arvore.append("    ");
            if (r.nivel[v] > 0) arvore.append("|-- ");
            arvore.append(m.g.nomes.get(v)).append("\n");
        }

        ArrayList<Integer> fora = new ArrayList<>();
        for (int v = 0; v < n; v++) if (!visitado[v]) fora.add(v);
        ArrayList<Integer> becos = Algoritmos.becosSemSaida(m.g);
        String nomeOrigem = m.g.nomes.get(s);

        String corpo = "<p><b>Ordem de visita:</b> " + ordem + "</p>"
                + "<p><b>Árvore DFS:</b></p><pre>" + arvore + "</pre>"
                + "<p><b>Conectividade:</b> "
                + (fora.isEmpty() ? "todos os vértices são alcançáveis a partir de " + nomeOrigem
                                  : nomesDe(fora, ", ") + " não alcançado(s) a partir de " + nomeOrigem)
                + "</p><p><b>Becos sem saída:</b> " + (becos.isEmpty() ? "nenhum" : nomesDe(becos, ", ")) + "</p>"
                + "<p><i>Complexidade: O(V + E)</i></p>";
        mostrarResultado("DFS a partir de " + nomeOrigem, corpo);
        m.animar(r.passos);
    }

    void mostrarBfs(int s) {
        Algoritmos.Busca r = Algoritmos.bfs(m.g, s);
        StringBuilder fila = new StringBuilder();
        ArrayList<String[]> linhas = new ArrayList<>();
        boolean[] alcancado = new boolean[m.g.numVertices()];
        for (int i = 0; i < r.quantos; i++) {
            int v = r.ordem[i];
            alcancado[v] = true;
            if (i > 0) fila.append(", ");
            fila.append(m.g.nomes.get(v));
            linhas.add(new String[]{m.g.nomes.get(v), "" + r.nivel[v],
                    nomesDe(Algoritmos.caminho(r.pai, v), " -&gt; ")});
        }
        ArrayList<Integer> fora = new ArrayList<>();
        for (int v = 0; v < m.g.numVertices(); v++) if (!alcancado[v]) fora.add(v);

        String corpo = "<p><b>Fila de execução:</b> " + fila + "</p>"
                + tabela(new String[]{"Vértice", "Saltos", "Caminho"}, linhas)
                + "<p>" + (fora.isEmpty() ? "Todos alcançados." : "Não alcançados: " + nomesDe(fora, ", ")) + "</p>"
                + "<p><i>Menor caminho em número de arestas. Complexidade: O(V + E)</i></p>";
        mostrarResultado("BFS a partir de " + m.g.nomes.get(s), corpo);
        m.animar(r.passos);
    }

    void mostrarDijkstra(int s) {
        Algoritmos.ResultadoDijkstra r;
        try {
            r = Algoritmos.dijkstra(m.g, s);
        } catch (IllegalArgumentException ex) {
            mostrarResultado("Erro", "<p style='color:red'><b>" + ex.getMessage() + "</b></p>");
            return;
        }
        ArrayList<String[]> linhas = new ArrayList<>();
        for (int v = 0; v < m.g.numVertices(); v++) {
            if (Double.isInfinite(r.dist[v])) {
                linhas.add(new String[]{m.g.nomes.get(v), "infinito", "sem caminho"});
            } else {
                linhas.add(new String[]{m.g.nomes.get(v), Algoritmos.formatar(r.dist[v]),
                        nomesDe(Algoritmos.caminho(r.pai, v), " -&gt; ")});
            }
        }
        String corpo = tabela(new String[]{"Vértice", "Distância", "Caminho"}, linhas)
                + "<p><i>Complexidade: O((V + E) log V), com heap binário</i></p>";
        mostrarResultado("Dijkstra a partir de " + m.g.nomes.get(s), corpo);
        m.animar(r.passos);
    }

    void mostrarKruskal() {
        Algoritmos.ResultadoKruskal r = Algoritmos.kruskal(m.g);
        ArrayList<String[]> linhas = new ArrayList<>();
        for (Algoritmos.ArestaKruskal e : r.analisadas) {
            linhas.add(new String[]{m.g.nomes.get(e.u) + " - " + m.g.nomes.get(e.v),
                    Algoritmos.formatar(e.peso), e.aceita ? "aceita" : "rejeitada (forma ciclo)"});
        }
        StringBuilder escolhidas = new StringBuilder();
        for (int i = 0; i < r.escolhidas.size(); i++) {
            Algoritmos.ArestaKruskal e = r.escolhidas.get(i);
            if (i > 0) escolhidas.append(", ");
            escolhidas.append(m.g.nomes.get(e.u)).append("-").append(m.g.nomes.get(e.v));
        }

        String corpo = "";
        if (m.g.direcionado) corpo += "<p><i>Grafo direcionado: as arestas foram tratadas como não-direcionadas.</i></p>";
        corpo += tabela(new String[]{"Aresta", "Peso", "Decisão"}, linhas)
                + "<p><b>Arestas escolhidas:</b> " + (escolhidas.length() == 0 ? "nenhuma" : escolhidas) + "</p>"
                + "<p><b>Custo total: " + Algoritmos.formatar(r.custoTotal) + "</b>"
                + (r.ligouTodos ? "" : " (grafo desconexo: floresta geradora mínima)") + "</p>"
                + "<p><i>Para ao escolher V-1 arestas. Complexidade: O(E log E)</i></p>";
        mostrarResultado("Kruskal (árvore geradora mínima)", corpo);
        m.animar(r.passos);
    }
}