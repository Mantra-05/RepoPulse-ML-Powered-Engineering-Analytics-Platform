# RepoPulse Implementation Walkthrough

I have successfully completed the implementation of all phases for the **RepoPulse** ML-Powered Engineering Analytics Platform.

## 1. Backend API (Java / Spring Boot)
- **Data Model:** Upgraded the JPA entities for `PullRequest`, `Repository`, and `Prediction` to correctly model the relationships. A new `RepositoryAnalysis` entity was introduced for storing computed repo metrics.
- **GitHub Integration:** Built out `GitHubService` utilizing a configured `RestTemplate` with the provided bearer token from `application.yml`. Handled paginated calls for Pull Requests, Commits, and Contributors.
- **Repository Analytics:** Implemented `RepositoryAnalysisService` to perform complex JPQL aggregations on PR sizes, files changed, and Java-side computations for review times and merge rates.
- **ML Integration:** Created a robust `PredictionService` that formats a 7-feature request payload, sends it to the FastAPI ML service (`POST /predict`), and handles fallbacks if the ML service is down.

## 2. ML Engine (Python / FastAPI)
- **Data Preprocessing Pipeline:** Created `ml/preprocess.py` to ingest raw CSV data, handle outliers with the IQR method, normalize features using `MinMaxScaler`, encode targets, and split the data securely.
- **Model Training:** Created `ml/train.py` utilizing `XGBClassifier` to predict Pull Request risk levels. It saves the serialized `.pkl` model, a scaler, and generates feature importance graphs and classification reports.
- **FastAPI Service:** Created `ml/main.py` offering a `POST /predict` endpoint that takes PR features, scales them, and returns calculated risk probabilities, review time estimations, and repository health indicators. 

## 3. Web Client (React / Vite / Tailwind)
- **Design System:** Bootstrapped a polished dark-themed application using TailwindCSS and Lucide-React icons, matching a professional glassmorphic and glowing design standard.
- **API Networking:** Wrapped up all backend endpoints into strongly typed `apiServices.ts` functions with a JWT interceptor auto-refresh capability on 401s.
- **Authentication:** Configured `AuthContext` providing automatic redirect loops and global state management for the signed-in user.
- **Core Pages:**
  - **Dashboard:** At-a-glance metrics covering total stars, forks, and repository highlights.
  - **Repositories:** Full CRUD with background GitHub sync triggers.
  - **Repository Detail & Analytics:** Integrated `Chart.js` via `react-chartjs-2` to display Doughnut charts (Health and Risk distributions) and Line/Bar charts (Review times, Merge rates).
  - **Pull Requests:** Extensive filtering and paginated viewing, including inline "Predict" actions to generate ML estimates instantly.
  - **Predictions:** Detailed view of risk levels, scores, priorities, and time estimates mapped directly back to the `pullRequestId`.

## Next Steps to Run Locally

You can run the three distinct services simultaneously in separate terminals:

1. **Spring Boot Backend**:
   ```bash
   cd backend
   # Ensure PostgreSQL is running and DB `repopulse` exists.
   export GITHUB_TOKEN="your_personal_access_token_here"
   mvn spring-boot:run
   ```

2. **Python ML Service**:
   ```bash
   cd ml
   pip install -r requirements.txt
   # Generate sample data and train the model
   python generate_dataset.py
   python preprocess.py
   python train.py
   # Start the service
   uvicorn main:app --host 0.0.0.0 --port 8001 --reload
   ```

3. **React Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
