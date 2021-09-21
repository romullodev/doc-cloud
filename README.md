![](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) ![](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white) ![](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white) ![](https://img.shields.io/badge/firebase-ffca28?style=for-the-badge&logo=firebase&logoColor=black)

# Aplicativo Doc Cloud
Este aplicativo é destinado ao gerenciamento e backup em nuvem de documentos no formato PDF. O registro dos documentos pode ser feito pela câmera do dispositivo ou pela seleção de fotos na galeria, com possibilidade de recorte das imagens obtidas. Além disso, o App foi projetado para ser funcional no modo offline, com sincronização automática após estabelecer conexão com a internt.

## Implementação
Este aplicativo foi desenvolido utilizando o padrão de design Model-View-ViewModel (MVVM), utilizando os conceitos do Single-Activity, com diversas bibliotecas do android Jetpack como, por exemplo, Navigation Component, ViewModel e Databinding. Além disso, este app utiliza injenção de dependência com Hilt e possui testes locais (com Robolectric) e instrumentados (com Espresso) em todas as telas. Para o salvar os documentos registrados, o app possui integração com o Firebase, utilizando o Realtime Database para o armazenamento dos dados e o Storage para os arquivos.

## Funcionalidades

**1** - Tela de Login: O usuário pode realizar o login com sua conta google ou pelo cadastramento de uma conta utilizando o seu email

**2** - Tela Inicial: Exibe todos os documentos cadastrados. O usuário pode registrar novos documentos utilizando a camera ou a seleção de imagens da galeria. Além disso, essa tela oferece opções para edição e compartilhamento.
    
**3** - Tela de Edição: Nesta tela, o usuário pode editar o nome do documento, além de excluir páginas específicas.

**4** - Tela da Câmera: Permite a captura das imagens com a Câmera do dispositivo, exibindo uma visisualização prévia (miniatura) das imagens obtidas.

**5** - Tela de Recorte: Após a obtenção das imagens (via câmera ou galeria), esta tela permite um ajuste de recorte das imagens para uma melhor adequação das páginas do documento.

## Projeto no Figma

	https://www.figma.com/file/MxQJS5OAlVs6CxFK2NqFnD/Doc-Cloud-App?node-id=0%3A1

## Screeshots

<p align="center">
  <img width="320" height="568" src=img/LoginScreen.jpeg>
</p>

<p align="center">
  <img width="320" height="568" src=img/LoginScreenLoading.jpeg>
</p>

<p align="center">
  <img width="320" height="568" src=img/HomeScreen.jpeg>
</p>

<p align="center">
  <img width="320" height="568" src=img/HomeScreenDate.jpeg>
</p>

<p align="center">
  <img width="320" height="568" src=img/HomeScreenCode.jpeg>
</p>

<p align="center">
  <img width="320" height="568" src=img/LoadScreen.jpeg>
</p>

<p align="center">
  <img width="320" height="568" src=img/OrderScreen.jpeg>
</p>
