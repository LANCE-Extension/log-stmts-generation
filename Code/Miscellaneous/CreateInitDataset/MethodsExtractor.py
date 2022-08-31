import logging
from bs4 import BeautifulSoup
import pandas as pd


class MethodsExtractor:
    def __init__(self, xml_file_path):
        self.xml_file = xml_file_path
        self.github_url = None

    def set_github_url(self, github_url: str):
        self.github_url = github_url

    def _extract_method_data(self, item_method, filename: str) -> pd.DataFrame:
        """
        Extract data from method
        Param: Method xml representation (item_method)
        Param: Filename of source Java file 
        Return: Dataframe containing the method extracted data
        """
        method_dict = {'github_url': self.github_url,
                       'file_path': filename,
                       'method_text': '',
                       'method_start': '',
                       'method_end': '',
                       'javadoc_text': '',
                       }
        # Get JavaDoc
        comment = item_method.find_previous_sibling()
        if comment and comment.name == 'comment' and comment.format == "javadoc":
            method_dict['javadoc_text'] = comment.get_text()

        # Remove inline comments
        for inline_comment in item_method.find_all('comment'):
            comment = inline_comment.extract()
        # Get method body w/o inline comments
        method_dict['method_text'] = item_method.get_text()
        # Get method body start and end 
        method_dict['method_start'] = item_method.get('pos:start')
        method_dict['method_end'] = item_method.get('pos:end')

        return pd.DataFrame().append(method_dict, ignore_index=True)

    def _extract_data(self, item_file):
        """
        Extract data from java file
        Param: Java file xml representation (item_file)
        Return: Dataframe containing the file per-method extrated data
        """
        methods_df_list = [self._extract_method_data(item_method, item_file.get('filename'))
                           for item_method in item_file.find_all('function')]
        if len(methods_df_list) > 0:
            return pd.concat(methods_df_list)
        else:
            return pd.DataFrame()

    def _get_dataframe(self, units) -> pd.DataFrame:
        """
        Extract data from java files
        Param: List of java files xml representations (units)
        Return: Dataframe containing all java files per-method extracted data
        """
        java_files_df_list = [self._extract_data(item_file) for item_file in units]
        if len(java_files_df_list) > 0:
            return pd.concat(java_files_df_list)
        else:
            return pd.DataFrame()

    def parse_xml_file(self) -> pd.DataFrame:
        """
        Parse xml file
        Return: Dataframe containing the extracted data for each 'function' in xml file
        """
        with open(self.xml_file, "r") as in_file:
            soup = BeautifulSoup(in_file.read(), 'lxml')
            github_repository_fullname = soup.unit.get('url').replace('cloned', '')
            github_url = f'https://github.com{github_repository_fullname}'
            self.set_github_url(github_url)
            units = soup.find_all('unit', {"language": "Java"})
            try:
                return self._get_dataframe(units)
            except ValueError as e:
                logging.error(f'Impossible to parse file {self.xml_file} due to {e}')
                return pd.DataFrame()
