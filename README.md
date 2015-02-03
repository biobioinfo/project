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

### Additional materials
- [Computing Symbolic Steady States of Boolean Networks](http://link.springer.com/chapter/10.1007%2F978-3-319-11520-7_59)
  Hannes Klarner, Alexander Bockmayr, Heike Siebert
- [Finding Attractors in Synchronous Multiple-Valued Networks Using SAT-based Bounded Model Checking](http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=5489232)
  Elena Dubrova, Liu Ming and Maxim Teslenko
- [Synchronous versus asynchronous modeling of gene regulatory networks](http://bioinformatics.oxfordjournals.org/content/24/17/1917.long)
  Abhishek Garg, Alessandro Di Cara, Ioannis Xenarios, Luis Mendoza and Giovanni De Micheli
- [LogicalModels](https://github.com/colomoto/logicalmodel)
  Aurélien Naldi

## TODO

- [x] Setup GitHub
- [ ] Implement the Boolean model for the network of blood stem cell using GINsim
  - [x] Specify the regulatory network
  - [x] Simulate the associated boolean model
  - [ ] Analyse the state transition
- [x] Implement a dummy plugin (i.e. check our comprehension of the organization of plugins)
- [ ] Implement the plugin for the analysis of the differentiation triggers
- [ ] Create the documentation
- [ ] Implement tests


## Idea

Try the implementation with other regulatory networks.


##Usage

The `script.sh` files copies the java files at the correct location. Before running it, you must either 
set the `GINSIM` variable to the project folder of GINsim or give the path to this folder as an argument
to the script.

Once this is done, GINsim must be recompiled to integrate the changes.

