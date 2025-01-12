FROM gitpod/workspace-full

USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install java 21.0.5-tem && \
    sdk default java 21.0.5-tem && \
    sdk install gradle 8.12 && \
    sdk default gradle 8.12"