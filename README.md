# STSFineGrain - a collection of STS models and a framework for their evaluation on fine-grained STS corpora
This package contains Java implementations of three baseline unsupervised STS models and four bag-of-words supervised STS models.
STSFineGrain includes the following models:

#### Unsupervised models
1. Word overlap
2. Mean of word2vec word vectors
3. Mixture of models 1 and 2

#### Supervised models
4. Islam and Inkpen
5. LInSTSS
6. POST STSS
7. POS-TF STSS

Please see the References section for papers describing all of the aforementioned models.

All models expect the input text to be formatted in UTF-8. The term frequency calculation output is also encoded in UTF-8, while model evaluation outputs are ANSI-encoded.

## Command-line interface
The supplied [STSFineGrain.jar](https://github.com/vukbatanovic/STSFineGrain/releases/download/v1.0.0/STSFineGrain.jar) file makes it possible to use the STSFineGrain framework from the command line.
The framework is invoked using the following general command form:

```
java -jar STSFineGrain.jar ActionID ActionSpecificArguments
```

ActionID can be:
* 0 - specifies that the action to be taken is the calculation of term frequency values.
* 1 - specifies that the action to be taken is the evaluation of an STS model.

### Term frequency calculation
If ActionID is 0, the command should the following form:
```
java -jar STSFineGrain.jar 0 InputCorporaPaths OutputTFPath
```

* *InputCorporaPaths* can contain arbitrarily many arguments - one path for each corpus file whose contents should be included in TF calculation.
* *OutputTFPath* can contain only one argument - the path to the file to which the calculated TF values will be written.

### STS model evaluation
If ActionID is 1, the command should have the following form:
```
java -jar STSFineGrain.jar 1 STSModelIndexNo EvaluationModeIndexNo LanguageCode STSCorpusRawTextsPath STSCorpusScoresPath Word2VecVectorsPath TermFrequenciesPath STSCorpusMSDorPOSPath
```

* *STSModelIndexNo* is an integer in the 1-7 range that specifies the STS model to be used. The mapping between index numbers and STS models is as follows:
    1. Word overlap
    2. Mean of word2vec word vectors
    3. Mixture of models 1 and 2
    4. Islam and Inkpen method
    5. LInSTSS
    6. POST STSS
    7. POS-TF STSS
* *EvaluationModeIndexNo* can be either 1 or 2:
    1. Evaluation is performed on the entire dataset specified through arguments STSCorpusRawTextPath and STSCorpusScoresPath. This is only suitable for unsupervised models.
    2. Evaluation is performed using cross-validation on the dataset specified through arguments STSCorpusRawTextPath and STSCorpusScoresPath. This is suitable for both supervised and unsupervised models. The standard number of CV folds is 10, but this setting can be changed in the code of the Evaluator class.
* *LanguageCode* - the two-letter ISO code specifying the language of the texts used. Currently, Serbian ("SR") and English ("EN") are supported.
* *STSCorpusRawTextsPath* - the path to the raw text file of the STS corpus to be used. Each line of the text file should contain the sentences of a pair, separated with a tab.
* *STSCorpusScoresPath* - the path to the score file of the STS corpus to be used. Each line of the file should contain the score of the corresponding line pair in the file specified by the *STSCorpusRawTextsPath* argument. The score file can also contain other information, but the score has to be the first item in a row, separated from other data with a tab.
* *Word2VecVectorsPath* - the path to the word2vec vector file that is used by all implemented STS models except the word overlap method. The vector file must be saved in the original C word2vec tool format. For the word overlap method, this argument is ignored.
* *TermFrequenciesPath* - the path to the term frequencies file created by this program. Term frequencies are only used for the LInSTSS and the POS-TF STSS models - for other models, this argument is ignored.
* *STSCorpusMSDorPOSPath* - the path to the STS corpus MSD/POS tags file. Each line of the text file should contain the tags of a pair of sentences, and sentences in a pair should be separated with a tab. The number of tags in a sentence should be identical to the number of tokens (not counting punctuation, which is eliminated in the text-cleaning phase). POS/MSD tags are only used for the POST STSS and the POS-TF STSS models - for other models, this argument is ignored.

## References
If you wish to use this package in your paper or project, please include a reference to the following paper in which it was presented:

**Fine-grained Semantic Textual Similarity for Serbian**, Vuk Batanović, Miloš Cvetanović, Boško Nikolić, in Proceedings of the 11th International Conference on Language Resources and Evaluation (LREC 2018), Miyazaki, Japan (2018).

Be sure to also cite the original paper of each STS model you use:
* For the word overlap model: *[SemEval-2012 Task 6: A Pilot on Semantic Textual Similarity](http://www.aclweb.org/anthology/S12-1051)*, Eneko Agirre, Daniel Cer, Mona Diab, Aitor Gonzalez-Agirre, in Proceedings of the First Joint Conference on Lexical and Computational Semantics (*SEM), Montreal, Canada, pp. 385–393 (2012).
* For the mean of word2vec word vectors model: *[SemEval-2017 Task 1: Semantic Textual Similarity Multilingual and Cross-lingual Focused Evaluation](http://www.aclweb.org/anthology/S17-2001)*, Daniel Cer, Mona Diab, Eneko Agirre, Iñigo Lopez-Gazpio, Lucia Specia, in Proceedings of the 11th International Workshop on Semantic Evaluations (SemEval-2017), Vancouver, Canada, pp. 1–14 (2017).
* For the word overlap and mean of word2vec word vectors mixture model: *Fine-grained Semantic Textual Similarity for Serbian*, Vuk Batanović, Miloš Cvetanović, Boško Nikolić, in Proceedings of the 11th International Conference on Language Resources and Evaluation (LREC 2018), Miyazaki, Japan (2018).
* For the Islam and Inkpen model: *[Semantic Text Similarity Using Corpus-Based Word Similarity and String Similarity](http://www.site.uottawa.ca/~diana/publications/tkdd.pdf)*, Aminul Islam, Diana Inkpen, ACM Transactions on Knowledge Discovery from Data, 2(2), Article No. 10 (2008).
* For the LInSTSS model: *[Semantic similarity of short texts in languages with a deficient natural language processing support](http://vukbatanovic.github.io/publication/dss_2013/)*, Bojan Furlan, Vuk Batanović, Boško Nikolić,  Decision Support Systems, 55(3), pp. 710–719 (2013).
* For the POST STSS model: *[Using Part-of-Speech Tags as Deep-Syntax Indicators in Determining Short-Text Semantic Similarity](http://vukbatanovic.github.io/publication/comsis_2015/)*, Vuk Batanović, Dragan Bojić, Computer Science and Information Systems, 12(1), pp. 1–31 (2015).
* For the POS-TF STSS model: *Fine-grained Semantic Textual Similarity for Serbian*, Vuk Batanović, Miloš Cvetanović, Boško Nikolić, in Proceedings of the 11th International Conference on Language Resources and Evaluation (LREC 2018), Miyazaki, Japan (2018).

## Additional Documentation
Some non-trivial parts of the source code contain comments and some documentation in English.
If you have any questions about the models' functioning, please review the source code, and the papers listed above.
If no answer can be found, feel free to contact me at: vuk.batanovic / at / student.etf.bg.ac.rs

## License
See the [license file](./LICENSE.md) for licensing information.
