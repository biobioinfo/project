# Boolean model for the network of the blood stem cell


## Presentation

This project is based on the articles mentioned below:

- [Hard-wired heterogeneity in blood stem cells revealed using a dynamic regulatory network model](http://bioinformatics.oxfordjournals.org/content/29/13/i80.long)
  Bonzanni N, Garg A, Feenstra KA, Schütte J, Kinston S, Miranda-Saavedra D, Heringa J, Xenarios I and Göttgens B
  Bioinformatics, 2014
- [An Efficient Algorithm for computing attractors of synchronous and asynchronous Boolean networks](http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0060593)
  Zheng D, Yang G, Li X, Wang Z, Liu F and He L
  PLoS One, 2013

The idea is to implement the Boolean model for the network controlling the blood
stem cell state as defined in Bonzanni et al (2014). A thorough comparative
analysis of the dynamics of this network using asynchronous versus synchronous
updating could then be performed, with the help of the following software:
[GINsim](http://ginsim.org). Next, the algorithms proposed in Zheng et al (2013)
could be implemented (either as independent code, or as a plugging for GINsim)
and evaluated on this model.


## TODO

- [ ] Setup GitHub
- [ ] Implement the Boolean model for the network of blood stem cell using GINsim
  - [ ] Specify the regulatory network
  - [ ] Simulate the associated boolean model
  - [ ] Analyse the state transition
- [ ] Implement a dummy plugin (i.e. check our comprehension of the organization of plugins)
- [ ] Implement the plugin for the analysis of the differentiation triggers
- [ ] Create the documentation
- [ ] Implement tests


## Idea

Try the implementation with other regulatory networks.

