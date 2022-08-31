# Extending and Improving Log Statements Generation Using Pre-trained Models

In this work, we intend to extend [LANCE](https://dl.acm.org/doi/10.1145/3510003.3511561) (the state-of-art approach able to synthesize complete log statements) and overcome some of its limitations. 
We begin by replicating LANCE on a dataset 3.6 times larger than the one used by Mastropaolo *et al.* 
Then, we experiment with different approaches to develop an extension of LANCE able to (i) discriminate whether a Java method requires additional log statements, (ii) take decisions about the number needed of log statements, and (iii) generate and inject multiple log statements.
Finally, we investigate the influence of injecting structural information into the code representation, by exploiting a code representation containing such information to replicate the training of all the experimented approaches.

**Repository Structure** 

- Set up a GCS Bucket
  - Before starting you need a new GCS Bucket. Follow the [original guide](https://cloud.google.com/storage/docs/discover-object-storage-console) by [Google](www.google.com).
  
- Code
  - *Pre-Training*: The code used to pre-train from scratch a new [T5 model]() is available at path [Code/Pre-Training]()
  - *Fine-Tuning*: The code used to fine-tune all the approaches is available at path [Code/Fine-Tuning]()
  - *Miscellaneous*: The folder at path []() contains the code used to: (i) clean and prepare the necessary datasets, (ii) inject structural information into Java code, (iii) data analysis, (iv) train the SentencePiece tokenizer, and (v) select the best-performing model performing early stopping.

- Datasets
  - All the datasets used in this work are available [HERE](). Note that for each train, eval, and test dataset we share both the TSV files required to train and evaluate the T5-based models and the CSV files containing additional information (*e.g.*, list of log statements contained by the method, list of the log statements stripped by the input sequence)
  
- Models (All the models with best performing configurations are publicly available)
  - Models trained using code representation without injected structural information
    - [Pre-Trained Model]()
    - [Replication of LANCE (Single LOG)]()
    - [Predicting the need for log statements Task1 (Multi LOG 0 to n)]()
    - [Predicting the need for log statements Task1 (T5-Based Classifier)]()
    - [Generating more than one log statement (Multi LOG 1 to n)]()
  - Models trained using code representation with injected structural information
    - [Pre-Trained Model (De-noise only task)]()
    - [Pre-Trained Model (Multi-task)]()
    - [Replication of LANCE (Single LOG)]()
    - [Predicting the need for log statements Task1 (Multi LOG 0 to n)]()
    - [Predicting the need for log statements Task1 (T5-Based Classifier)]()
    - [Generating more than one log statement (Multi LOG 1 to n)]()
- Results: 
  - Predictions
    - [Single LOG]()
    - [Multi LOG 0 to n]()
    - [T5-Based Classifier]()
    - [Multi LOG 1 to n]()
    - [Single LOG with structural information]()
    - [Multi LOG 0 to n with structural information]()
    - [T5-Based Classifier with structural information]()
    - [Multi LOG 1 to n with structural information]()
  - Manual Analysis
    - Single LOG
