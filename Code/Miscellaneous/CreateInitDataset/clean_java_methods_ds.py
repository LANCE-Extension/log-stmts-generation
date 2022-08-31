import argparse
import logging
import pandas as pd
import javalang as jl
import multiprocessing as mp


def main(arguments):
    logging.info("Loading file...")
    filename = arguments.file
    if filename.endswith(".pkl"):
        raw_df = pd.read_pickle(filename)
    else:
        raw_df = pd.read_csv(filename)

    logging.info(f'Loading file ended, at: {filename}')
    print('Init shape:', raw_df.shape)

    logging.info(f'Drop NA in method_text')
    nona_df = raw_df.dropna(subset=['method_text'])

    logging.info(f'Tokenize method text')
    with mp.Pool(16) as pool:
        nona_df['tokenized_method_text'] = pool.map(_tokenize_method_text, nona_df.method_text)

    logging.info(f'Drop NA in tokenized_method_text')
    nona_tokenized_methods_df = nona_df.dropna(subset=['tokenized_method_text'])

    logging.info(f'Count tokens in tokenized method text')
    with mp.Pool(16) as pool:
        nona_tokenized_methods_df['tokens_number'] = pool.map(len, nona_tokenized_methods_df.tokenized_method_text)

    logging.info(f'Drop duplicates')
    nodup = nona_tokenized_methods_df.drop_duplicates(subset=['tokenized_method_text'], keep='first').reset_index(
        drop=True)

    logging.info(f'Filter by # tokens')
    filtered_tokens_df = _filter_by_number_of_token(nodup)

    print('Final shape:', filtered_tokens_df.shape)
    filtered_tokens_df.to_csv(arguments.output)


def _tokenize_method_text(text: str) -> str:
    """
    Tokenize (using javalang tokenizer) the input method text
    Param: Original method text
    Return: Tokenized method text
    """
    try:
        return " ".join([token.value.encode('utf8', 'ignore')
                        .decode('utf8', 'ignore') for token in jl.tokenizer.tokenize(str(text))])
    except (jl.tokenizer.LexerError, TypeError):
        pass


def _filter_by_number_of_token(df: pd.DataFrame) -> pd.DataFrame:
    """
  Filter out entries based on method text tokens number
  Param: Original Dataframe 
  Return: Dataframe w/o method entries w/ #tokens <= 10 and #tokens > 512
  """
    df = df[df.tokens_number > 10]
    return df[df.tokens_number <= 512]


# def _filter_custom_log_level(df: pd.DataFrame) -> pd.DataFrame:
#     """
#   Filter out entries with log statements using custom log levels
#   Param: Origial Dataframe
#   Return: Dataframe w/o method entries w/ custom log levels
#   """
#     df['has_custom'] = df.log_level.apply(_has_custom_level)
#     custom_lvl_df = df[df.has_custom]
#     no_custom_lvl_df = df[~df.has_custom]
#     return no_custom_lvl_df.drop(columns=['has_custom'])


# def _has_custom_level(levels: "list[str]") -> bool:
#     """
#   Check if method entry has logs w/ custom log levels
#   Param: List of all the log levels in method entry
#   Return: True id method entry has logs w/ custom levels, otherwise False
#   """
#     std_levels = ['fatal', 'error', 'warn', 'debug', 'info', 'trace']
#     for level in levels:
#         if level not in std_levels:
#             return True
#     return False


def _setup_arguments_parser():
    """
    Init ArgumentParser w/ specific input arguments
    Return: ArgumentParser
    """
    args_parser = argparse.ArgumentParser()
    args_parser.add_argument('--file',
                             dest='file',
                             type=str,
                             help='Path of input .pkl file.')
    args_parser.add_argument('--out',
                             dest='output',
                             type=str, nargs='?',
                             const='out/initial-ds/java_methods_clean.csv',
                             default='out/initial-ds/java_methods_clean.csv',
                             help='Path/filename.csv for output clean dataset. '
                                  'default="out/initial-ds/java_methods_clean.csv"')
    return args_parser


if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)
    parser = _setup_arguments_parser()
    args = parser.parse_args()
    main(args)
