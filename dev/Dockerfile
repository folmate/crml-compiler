FROM lscr.io/linuxserver/rdesktop:ubuntu-xfce

ENV PUID=1000 
ENV PGID=1000
ENV TZ=Etc/UTC 

EXPOSE 3389

RUN apt update
RUN apt install git -y
RUN apt install wget -y
RUN apt install openjdk-8-jdk -y

#COPY ssh keys

RUN wget https://download-cdn.jetbrains.com/idea/ideaIC-2023.3.6.tar.gz
RUN tar -xzf ideaIC-*.tar.gz -C /opt
RUN rm ideaIC-2023.3.6.tar.gz

RUN echo '[Desktop Entry]\n\
Version=13.0\n\
Type=Application\n\
Terminal=false\n\
Icon[en_US]=/opt/idea-IC-233.15026.9/bin/idea.png\n\
Name[en_US]=IntelliJ\n\
Exec=/opt/idea-IC-233.15026.9/bin/idea.sh\n\
Name=IntelliJ\n\
Categories=Development\n\
Icon=/opt/idea-IC-233.15026.9/bin/idea.png' > /usr/share/applications/intellij.desktop

RUN apt install ca-certificates curl gnupg
RUN curl -fsSL http://build.openmodelica.org/apt/openmodelica.asc | gpg --dearmor -o /usr/share/keyrings/openmodelica-keyring.gpg

RUN echo "deb [arch=amd64 signed-by=/usr/share/keyrings/openmodelica-keyring.gpg] \
  https://build.openmodelica.org/apt \
  $(cat /etc/os-release | grep "\(UBUNTU\\|DEBIAN\\|VERSION\)_CODENAME" | sort | cut -d= -f 2 | head -1) \
  stable" | sudo tee /etc/apt/sources.list.d/openmodelica.list

RUN apt update
RUN apt install --no-install-recommends omc -y

COPY .ssh /config/.ssh
