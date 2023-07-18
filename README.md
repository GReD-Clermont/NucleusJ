# NucleusJ2.0 


NucleusJ 2.0 is a new release of [NucleusJ](https://github.com/PouletAxel/NucleusJ_), in which image processing is achieved more quickly using a command-line user interface. Starting with large collection of 3D nuclei, segmentation can be performed by the previously developed Otsu-modified method or by a new 3D gift-wrapping method, taking better account of nuclear indentations and unmarked nucleoli.

A discrete geometric method was introduced to improve the surface area calculation, a key parameter when studying nuclear morphology, replacing an imageJ default tool by a new one that includes pixel context information.

To increase the number of nuclei considered in a single analysis, a method was introduced to delimit an automatic bounding volume (autocrop) around each nucleus of a 3D wide-field stack containing ten to a hundred nuclei. Each of the collected nuclei can then be segmented through two complementary methods, either based on the Otsu threshold method or on edge-detection through a 3D gift-wrapping method.

[Article direct link](https://www.tandfonline.com/doi/full/10.1080/19491034.2020.1845012)

# What's new

The gift wrapping algorithm has been replaced by a graham scan algorithm which is more efficient. Thus, the speed of the segmentation is enhanced.

# Download and manual user links

Download the latest version: [jar](https://github.com/GReD-Clermont/NucleusJ2/releases/latest)

Manual user and examples: [wiki](https://github.com/GReD-Clermont/NucleusJ2/wiki)

# Authors & contact

Axel Poulet

Dubos Tristan

Contact: tristan.duos33@gmail.com

# How to cite :

Tristan Dubos, Axel Poulet, Céline Gonthier-Gueret, Guillaume Mougeot, Emmanuel Vanrobays, Yanru Li, Sylvie Tutois, Emilie Pery, Frédéric Chausse, Aline V. Probst, Christophe Tatout & Sophie Desset (2020) Automated 3D bio-imaging analysis of nuclear organization by NucleusJ 2.0, Nucleus, 11:1, 315-329, DOI: 10.1080/19491034.2020.1845012 
