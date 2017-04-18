FROM conservify/arm-jdk8-oracle

COPY firmware-tool-cli-1.0-SNAPSHOT.tar /app/firmware-tool-cli.tar

WORKDIR /app

RUN tar xf firmware-tool-cli.tar
RUN mv firmware-tool-cli-* firmware-tool-cli && chmod 755 firmware-tool-cli/bin/firmware-tool-cli && find
RUN chmod 755 /app/firmware-tool-cli/tools/bossac_linux_arm

CMD ["/app/firmware-tool-cli/bin/firmware-tool-cli"]

