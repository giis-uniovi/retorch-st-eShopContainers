FROM envoyproxy/envoy:v1.11.1

ARG TAG

ADD "./tmp/${TAG}/mobileshopping/envoy.yaml" "/etc/envoy/envoy.yaml"

LABEL version="${TAG}"
