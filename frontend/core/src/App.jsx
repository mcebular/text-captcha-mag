import React from "react";
import ApiClient from "./ApiClient";
import style from "./App.scss";
import CaptchaTask from "./CaptchaTask";
import Start from "./Start";

function getArticleUrl() {
  return window.location.hostname + window.location.pathname;
}

export const AppState = {
  INGEST_LOADING: 1,
  INGEST_DONE: 2,
  INGEST_ERROR: 3,

  TASK_LOADING: 4,
  TASK_SHOW: 5,
  TASK_SUBMITTING: 6,
  TASK_DONE: 7,
  TASK_ERROR: 8,
};

class App extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      currentState: AppState.INGEST_LOADING,

      ingestData: null,
      captchaTask: null,
      captchaTaskResponse: null,
    };
  }

  componentDidMount() {
    this.makeIngestRequest();
  }

  makeIngestRequest = async () => {
    try {
      const data = await ApiClient.ingest(
        getArticleUrl(),
        this.props.getText()
      );
      this.setState({
        currentState: AppState.INGEST_DONE,
        ingestData: data,
      });
    } catch (e) {
      this.setState({
        currentState: AppState.INGEST_ERROR,
      });
    }
  };

  makeTaskRequest = async () => {
    this.setState({ currentState: AppState.TASK_LOADING });
    try {
      const data = await ApiClient.task.request(this.state.ingestData);
      console.log(data);
      this.setState({
        currentState: AppState.TASK_SHOW,
        captchaTask: data,
      });
    } catch (e) {
      this.setState({ currentState: AppState.TASK_ERROR });
    }
  };

  makeTaskResponse = async (selectedIndexes) => {
    this.setState({ currentState: AppState.TASK_SUBMITTING });
    try {
      const data = await ApiClient.task.response(
        this.state.captchaTask.id,
        selectedIndexes
      );
      console.log(data);
      this.setState({
        currentState: AppState.TASK_DONE,
        captchaTask: null,
        captchaTaskResponse: data,
      });
    } catch (e) {
      this.setState({ currentState: AppState.TASK_ERROR });
    }
  };

  renderContent = () => {
    const { currentState } = this.state;

    switch (currentState) {
      case AppState.INGEST_LOADING:
        return "Poteka obdelava teksa in priprava nalog...";

      case AppState.INGEST_DONE:
        return (
          <Start
            buttonText="Reši nalogo"
            onStart={() => {
              this.makeTaskRequest();
            }}
          />
        );

      case AppState.INGEST_ERROR:
        return "Prišlo je do napake.";

      case AppState.TASK_LOADING:
      case AppState.TASK_SHOW:
      case AppState.TASK_SUBMITTING:
        return (
          <CaptchaTask
            task={this.state.captchaTask}
            isLoading={
              this.state.currentState === AppState.TASK_LOADING ||
              this.state.currentState === AppState.TASK_SUBMITTING
            }
            onSubmit={(selectedIndexes) => {
              this.makeTaskResponse(selectedIndexes);
            }}
          />
        );

      case AppState.TASK_DONE:
        return (
          <Start
            buttonText="Reši novo nalogo"
            onStart={() => {
              this.makeTaskRequest();
            }}
            content={this.state.captchaTaskResponse.content}
          />
        );

      case AppState.TASK_ERROR:
        return (
          <Start
            buttonText="Reši novo nalogo"
            onStart={() => {
              this.makeTaskRequest();
            }}
            content={"Prišlo je do napake."}
          />
        );

      default:
        console.warn(
          "Trying to render content for undefined state: ",
          currentState
        );
        return null;
    }
  };

  render() {
    return (
      <div className={style["tc-main"]}>
        <div className={style["tc-content"]}>{this.renderContent()}</div>
        <div className={style["tc-footer"]}>TextCAPTCHA 0.1</div>
      </div>
    );
  }
}

export default App;