import os
from tqdm import tqdm
import logging
import argparse
import pandas as pd
from MethodsExtractor import MethodsExtractor


def main(arguments):
    logging.info('Get xml files paths')
    xml_files_paths = _get_xml_files_path(arguments.path)

    logging.info('Process xml files')
    per_file_df_list = _process_xml_files(xml_files_paths)

    # concat all the per-file dataframes
    merged_df = pd.concat(per_file_df_list)

    logging.info('Save dataset sample in .csv')
    merged_df.sample(100).to_csv(f'{arguments.output}-sample.csv', index=False)
    logging.info('Saving dataset...')
    f_out = arguments.output
    if f_out.endswith('pkl'):
        merged_df.to_pickle(f_out)
    else:
        merged_df.to_csv(f_out)
    logging.info(f'Saving ended, at: {arguments.output}')


def _get_xml_files_path(root: str) -> "list[str]":
    """
    Get xml files paths
    Param: Path of cloned repositories root folder
    Return: List of srcml output xml files paths
    """
    files_paths = []
    for item in os.listdir(root):
        item_path = os.path.join(root, item)
        if os.path.isdir(item_path):
            for sub_item in os.listdir(item_path):
                sub_item_path = os.path.join(root, item, sub_item)
                if os.path.isfile(sub_item_path) and sub_item.endswith('.srcml.xml'):
                    files_paths.append(sub_item_path)
    return files_paths


def _process_xml_files(xml_files_paths: "list[str]") -> "list[pd.DataFrame]":
    """
    Process the xml files in parallel
    Parameter: List of srcml output xml files paths
    Return: List of dataframes, one for each xml file to process
    """
    return [MethodsExtractor(xml_file_path).parse_xml_file() for xml_file_path in tqdm(xml_files_paths)]


def _setup_arguments_parser():
    """
    Init ArgumentParser w/ specific input arguments
    Return: ArgumentParser
    """
    args_parser = argparse.ArgumentParser(description='Create raw dataset from srcml output xml files.')
    args_parser.add_argument('--path',
                             dest='path',
                             type=str,
                             help='Path to reach xml files.')
    args_parser.add_argument('--out',
                             dest='output',
                             type=str, nargs='?',
                             const='out/initial-ds/java_methods_full.csv', default='out/initial-ds/java_methods_full'
                                                                                   '.csv',
                             help='Path/filename.csv for output raw dataset. '
                                  ' default="out/initial-ds/java_methods_full.csv"')
    return args_parser


if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)
    parser = _setup_arguments_parser()
    args = parser.parse_args()
    main(args)
