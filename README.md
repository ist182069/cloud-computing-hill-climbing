# [Hill Climbing Algorithm Cloud Computing Solver](https://fenix.tecnico.ulisboa.pt/disciplinas/AVExe/2018-2019/2-semestre)

## Notas Iniciais
O objectivo desde projecto foi criar um sistema que recebia informações de uma front end com o objectivo de computar coordenadas em gráficos para calcular alturas consoante diversos tipos possíveis de algoritmos (Astar, BFS, DFS). A nota deste projecto foi 17.8/20. Este projecto foi avaliado pelo responsável da cadeira, o Professor Luís Veiga, e pelo Bolseiro Nuno Anselmo.

## Módulos
- *./Enunciado* contém o enunciado deste projecto.
- O Auto Scaler inicializa e termina Web Servers.
- O ponto de entrada do sistema é o LoadBalancer.
- O Load Balancer envia o pedido recebido ao Web Server.
- O Web Server processa o pedido e devolve o resultado ao Load Balancer e guarda as métricas no Dynamo. 
- O Load Balancer retorna o resultado.
- As configurações do sistema estão localizadas no ficheiro rc.local

## Grupo 11
- 82069 - José Brás - MEIC-T (Classificação final obtida: 16/20 valores) https://github.com/ist182069
- 84609 - Marco Coelho - MEIC-T (Classificação final obtida: 16/20 valores) https://github.com/OCoise
- 84897 - Dário Sá - MEIC-T (Classificação final obtida: 15/20 valores) https://github.com/dariosa
