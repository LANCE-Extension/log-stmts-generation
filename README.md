# Extending and Improving Log Statements Generation Using Pre-trained Models

In this work, we intend to extend [LANCE](https://dl.acm.org/doi/10.1145/3510003.3511561) (the state-of-art approach able to synthesize complete log statements) and overcome some of its limitations. 
We begin by replicating LANCE on a dataset 3.6 times larger than the one used by Mastropaolo *et al.* 
Then, we experiment with different approaches to develop an extension of LANCE able to (i) discriminate whether a Java method requires additional log statements, (ii) take decisions about the number needed of log statements, and (iii) generate and inject multiple log statements.
Finally, we investigate the influence of injecting structural information into the code representation, by exploiting a code representation containing such information to replicate the training of all the experimented approaches.

**Repository Structure** 

- Set up a GCS Bucket
  - Before starting you need a new GCS Bucket. Follow the [original guide](https://cloud.google.com/storage/docs/discover-object-storage-console) by [Google](www.google.com).
  
- Code
  - *Pre-Training*: The code used to pre-train from scratch a new [T5 model]() is available at path [Code/Pre-Training](https://github.com/LANCE-Extension/log-stmts-generation/tree/main/Code/Pre-Training)
  - *Fine-Tuning*: The code used to fine-tune all the approaches is available at path [Code/Fine-Tuning](https://github.com/LANCE-Extension/log-stmts-generation/tree/main/Code/Fine-Tuning)
  - *Miscellaneous*: The folder at path [Code/Miscellaneous](https://github.com/LANCE-Extension/log-stmts-generation/tree/main/Code/Miscellaneous) contains the code used to: (i) clean and prepare the necessary datasets, (ii) inject structural information into Java code, (iii) data analysis, (iv) train the SentencePiece tokenizer, and (v) select the best-performing model performing early stopping.

- Datasets
  - All the datasets used in this work are available [HERE](https://drive.google.com/drive/folders/19cuEhrALm0INgm8lAvt_dt58y69yxqq4?usp=sharing). Note that for each train, eval, and test dataset we share both the TSV files required to train and evaluate the T5-based models and the CSV files containing additional information (*e.g.*, list of log statements contained by the method, list of the log statements stripped by the input sequence)
  
- Models (All the models with best performing configurations are publicly available)
  - Models trained using code representation without injected structural information
    - [SentencePiece Model / Vocab](https://drive.google.com/drive/folders/117BwUoIqVHQAGaHFRbUiTIBGLlS598gL?usp=sharing)
    - [Pre-Trained Model](https://drive.google.com/drive/folders/1-4tqq5vyoe1YASfhiINfeXYcp_rbCagT?usp=sharing)
    - [Replication of LANCE (Single LOG)](https://drive.google.com/drive/folders/1-KTjykmqJmyJK8_Tcd6rj9Wvp1GdrdsI?usp=sharing)
    - [Predicting the need for log statements Task1 (Multi LOG 0 to n)](https://drive.google.com/drive/folders/10a8tp9RctENYb9VcZtdUS-QIEI6w3bEU?usp=sharing)
    - [Predicting the need for log statements Task2 (T5-Based Classifier)](https://drive.google.com/drive/folders/1-iMYuCefRLvSTxbgvrsoEu9F29o2rjjh?usp=sharing)
    - [Generating more than one log statement (Multi LOG 1 to n)](https://drive.google.com/drive/folders/10wxWExrqD2Sbty1lUuynfr1uzEPJgJjD?usp=sharing)
  - Models trained using code representation with injected structural information
    - [SentencePiece Model / Vocab](https://drive.google.com/drive/folders/11DOIiiM0CokBG2ztLul7eeW4FTqLEjGr?usp=sharing)
    - [Pre-Trained Model (De-noise only task)](https://drive.google.com/drive/folders/11ZdGem8MImDOzRhtEdE3ixMwSUGDCd7s?usp=sharing)
    - [Pre-Trained Model (Multi-task)](https://drive.google.com/drive/folders/12AHEPCtWEy0rqdadoeXTM33WiUzdSV7-?usp=sharing)
    - [Replication of LANCE (Single LOG)](https://drive.google.com/drive/folders/12W89GUl94zH-WP0VgtslLLeLcnoUpGLc?usp=sharing)
    - [Predicting the need for log statements Task1 (Multi LOG 0 to n)](https://drive.google.com/drive/folders/12xg4UKT14N7Nd8AO-b8O4_b_8SosgqDE?usp=sharing)
    - [Predicting the need for log statements Task2 (T5-Based Classifier)](https://drive.google.com/drive/folders/13tdd1ujCm4UWKNURjy0pwaADWwEK28lD?usp=sharing)
    - [Generating more than one log statement (Multi LOG 1 to n)](https://drive.google.com/drive/folders/13Rr3fUl_szFDNQrxicxz7ftaBgVoT7d-?usp=sharing)
- Results: 
  - Predictions
    - [Single LOG](https://drive.google.com/drive/folders/1Dd1H1M9S7EzK0r6O0xWdHPvMzQibm-vU?usp=sharing)
    - [Multi LOG 0 to n]()
    - [T5-Based Classifier](https://drive.google.com/drive/folders/1P8TZYbmb396tZDU8vFwTRInSKbttI6-Z?usp=sharing)
    - [Multi LOG 1 to n]()
    - [Single LOG with structural information]()
    - [Multi LOG 0 to n with structural information]()
    - [T5-Based Classifier with structural information]()
    - [Multi LOG 1 to n with structural information]()
  - Manual Analysis
    - [Single LOG](https://drive.google.com/file/d/1rm1qngEY-7KfRD-nrsRaqYGpS6vfuXSH/view?usp=sharing)
