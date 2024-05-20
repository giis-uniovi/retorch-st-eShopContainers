FROM envoyproxy/envoy:v1.11.1

ARG TAG
ADD "ApiGateways/Envoy/config/webshopping/envoy.yaml" "/etc/envoy/envoy.yaml"
RUN sed -i "s/\${tjobname}/$TAG/g" /etc/envoy/envoy.yaml
LABEL version="${TAG}"