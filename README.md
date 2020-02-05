# CNV

**IREI FAZER UPDATE DO MARKDOWN NO FUTURO** 

O Auto Scaler inicializa e termina Web Servers.
O ponto de entrada do sistema é o LoadBalancer.
O Load Balancer envia o pedido recebido ao Web Server.
O Web Server processa o pedido e devolve o resultado ao Load Balancer e guarda as métricas no Dynamo. 
O Load Balancer retorna o resultado.

As configurações do sistema estão localizadas no ficheiro rc.local
