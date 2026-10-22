# SmartNet Analyzer
- **Curso:** Engenharia de Computação/Software, IBMEC
- **Disciplina:** Análise e Complexidade de Algoritmos (IBM3121), 2026.2
- **Professor:** Cassius Figueiredo
- **Integrantes (nome e matrícula):**
  - _Nome 1_ — _matrícula_
  - _Nome 2_ — _matrícula_

## Sobre
Aplicação web (HTML + JavaScript puro, sem dependências) que modela uma cidade como grafo e executa DFS, BFS, Dijkstra e Kruskal. Entrada e saída são visuais: o grafo é desenhado no quadro e os resultados são destacados nele, além de tabelas.Aplicação em produção no link a seguir: https://thiagobrandaorj.github.io/Smart-cities-graph/

## Como executar
1. Baixe/clone o repositório.
2. Abra `index.html` em um navegador moderno (Chrome, Firefox, Edge). Nenhuma instalação é necessária.
   Opcional: `python3 -m http.server` e acesse `http://localhost:8000`.
3. Para rodar os testes dos algoritmos: `node tests.js`.

## Como usar
- **Vértice:** clique no quadro. Duplo clique em um vértice para renomeá-lo.
- **Aresta:** clique na origem e depois no destino (informe o peso quando ponderado).
- **Mover / Apagar:** reposicione ou remova vértices e arestas.
- **Direcionado / Ponderado:** alternam o tipo do grafo a qualquer momento.
- Escolha a **origem**, clique em DFS, BFS, Dijkstra ou Kruskal. O resultado é animado no grafo e listado em texto/tabela.
- "Carregar grafo do enunciado" insere o grafo de teste do PDF. "Apagar grafo" limpa tudo. "Finalizar aplicação" encerra.

## Estrutura de dados
**Lista de adjacência** implementada do zero (`Map` de vértice para array de `{to, w}`), exibida na tela. Escolhida porque grafos urbanos são esparsos: memória O(V+E) contra O(V²) da matriz, e DFS/BFS em O(V+E).

## Complexidades
| Algoritmo | Complexidade |
|---|---|
| DFS / BFS | O(V+E) |
| Dijkstra (heap binário implementado do zero) | O((V+E) log V) |
| Kruskal (ordenação + Union-Find com rank e compressão) | O(E log E) |

## Observações
- Dijkstra rejeita pesos negativos com mensagem de erro.
- Kruskal em grafo direcionado trata as arestas como não-direcionadas; em grafo desconexo devolve a floresta geradora mínima.
- Vértices e arestas inexistentes/auto-laços são ignorados.
